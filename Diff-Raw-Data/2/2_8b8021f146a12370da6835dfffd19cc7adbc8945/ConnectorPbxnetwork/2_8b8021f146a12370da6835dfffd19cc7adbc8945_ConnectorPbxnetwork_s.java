 /*
  * Copyright (C) 2010 Lorenz Bauer, Felix Bechstein
  * 
  * This file is part of WebSMS.
  * 
  * This program is free software; you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation; either version 3 of the License, or (at your option) any later
  * version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * this program; If not, see <http://www.gnu.org/licenses/>.
  */
 package com.lmb.android.websms.pbxnetwork;
 
 import java.net.SocketTimeoutException;
 import java.text.DecimalFormat;
 import java.util.Currency;
 import java.util.Vector;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.preference.PreferenceManager;
 import de.ub0r.android.websms.connector.common.Connector;
 import de.ub0r.android.websms.connector.common.ConnectorCommand;
 import de.ub0r.android.websms.connector.common.ConnectorSpec;
 import de.ub0r.android.websms.connector.common.Log;
 import de.ub0r.android.websms.connector.common.Utils;
 import de.ub0r.android.websms.connector.common.WebSMSException;
 import de.ub0r.android.websms.connector.common.ConnectorSpec.SubConnectorSpec;
 
 /**
  * Receives commands coming as broadcast from WebSMS.
  * 
  * @author lmb
  */
 public class ConnectorPbxnetwork extends Connector {
 	/** Tag for debug output. */
 	private static final String TAG = "pbxnetwork";
 	private static final BillingService BILLING_SERVICE = new BillingService();
 	private static final SMSService SMS_SERVICE = new SMSService();
 	public static final NumberService NUMBER_SERVICE = new NumberService();
 	public static final Ticket TICKET = new Ticket();
 	private static final String ID_DEFAULT = "default";
 	private static final String ID_LOWCOST = "lowcost";
 
 	protected boolean mSenderIsValid = true;
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public final ConnectorSpec initSpec(final Context context) {
 		final String name = context
 				.getString(R.string.connector_pbxnetwork_name);
 		ConnectorSpec c = new ConnectorSpec(name);
 		c.setAuthor(// .
 				context.getString(R.string.connector_pbxnetwork_author));
 		c.setBalance(null);
 		c.setCapabilities(ConnectorSpec.CAPABILITIES_BOOTSTRAP
 				| ConnectorSpec.CAPABILITIES_UPDATE
 				| ConnectorSpec.CAPABILITIES_SEND
 				| ConnectorSpec.CAPABILITIES_PREFS);
 		c.addSubConnector(ID_DEFAULT, context
 				.getString(R.string.connector_default),
 				SubConnectorSpec.FEATURE_NONE);
 		c.addSubConnector(ID_LOWCOST, context
 				.getString(R.string.connector_lowcost),
 				SubConnectorSpec.FEATURE_NONE);
 		return c;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public final ConnectorSpec updateSpec(final Context context,
 			final ConnectorSpec connectorSpec) {
 		final SharedPreferences p = PreferenceManager
 				.getDefaultSharedPreferences(context);
 		if (p.getBoolean(Preferences.PREFS_ENABLED, false)
 				&& p.getString(Preferences.PREFS_USER, "").length() > 0
 				&& p.getString(Preferences.PREFS_PASSWORD, "").length() > 0
 				&& this.mSenderIsValid) {
 			connectorSpec.setReady();
 		} else {
 			connectorSpec.setStatus(ConnectorSpec.STATUS_INACTIVE);
 		}
 		return connectorSpec;
 	}
 
 	@Override
 	protected final void doBootstrap(final Context context, final Intent intent) {
 		Log.i(TAG, "bootstrap");
 
 		this.mSenderIsValid = this.verifySender(context, intent);
 
 		if (!this.mSenderIsValid) {
 			String message = String.format(context
 					.getString(R.string.err_unregistered_extension), this
 					.getSender(context, intent));
 
 			throw new WebSMSException(message);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected final void doUpdate(final Context context, final Intent intent) {
 		Log.i(TAG, "update");
 
 		ConnectorSpec cs = this.getSpec(context);
 
 		Double credit = 0.0;
 		try {
 			credit = BILLING_SERVICE.getCredit(TICKET.get(context));
 		} catch (SocketTimeoutException e) {
 			throw new TimeoutException(context, R.string.err_balance);
 		}
 
		DecimalFormat formatter = new DecimalFormat("#0.00¤");
 		formatter.setCurrency(Currency.getInstance("EUR"));
 
 		cs.setBalance(formatter.format(credit));
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected final void doSend(final Context context, final Intent intent) {
 		ConnectorCommand cc = new ConnectorCommand(intent);
 
 		String sender = Utils.getSender(context, cc.getDefSender());
 		Log.i(TAG, "Sending with sender " + sender);
 
 		String message = cc.getText();
 		String[] recipients = cc.getRecipients();
 		String recipient;
 
 		recipient = Utils.national2international(cc.getDefPrefix(), Utils
 				.getRecipientsNumber(recipients[0]));
 
 		String subconnector = cc.getSelectedSubConnector();
 		if (subconnector == null) {
 			// FIXME: Can this even happen?
 			throw new RuntimeException("No subconnector was selected!");
 		}
 
 		SoapTypeTicket ticket = TICKET.get(context);
 		try {
 			if (subconnector.equals(ID_DEFAULT)) {
 				SMS_SERVICE.sendSMS(sender, recipient, message, ticket);
 			} else {
 				if (message.length() > 160) {
 					throw new WebSMSException(context,
 							R.string.err_msg_too_long);
 				}
 
 				SMS_SERVICE.sendLowCostSMS(recipient, message, ticket);
 			}
 		} catch (SocketTimeoutException e) {
 			throw new TimeoutException(context, R.string.err_sending);
 		}
 
 		Log.i(TAG, "Sent message");
 	}
 
 	protected boolean verifySender(final Context context, final Intent intent) {
 
 		Vector<String> extensions = null;
 		try {
 			extensions = NUMBER_SERVICE.getUserExtensions(TICKET.get(context));
 		} catch (SocketTimeoutException e) {
 			// SOAP call failed.
 			return true;
 		}
 
 		String sender = this.getSender(context, intent);
 		return extensions.contains(sender);
 	}
 
 	protected final String getSender(final Context context, final Intent intent) {
 		return Utils.getSender(context, new ConnectorCommand(intent)
 				.getDefSender());
 	}
 }
