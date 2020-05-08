 /*
  * Copyright (C) 2012 BeeOne GmbH
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package at.beeone.netbankinglight.util;
 
 import at.beeone.netbankinglight.api.model.*;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 
 public class JsonParser {
 
 	private static String getString(JSONObject jsonObject, String propertyName)
 			throws JSONException {
 		String value = jsonObject.getString(propertyName);
 		return value.trim().length() > 1 ? value : null;
 	}
 
 	public static AccountImpl toAccount(JSONObject accountObj)
 			throws JSONException {
 		AccountImpl account = new AccountImpl();
 		String id = accountObj.getString("id");
 		String type = accountObj.getString("type");
 		Long balance = accountObj.getLong("balance");
 		String iban = accountObj.getString("iban");
 		Long overdraft = accountObj.getLong("overdraft");
 		Long availableFunds = accountObj.getLong("availableFunds");
 
 		if (accountObj.has("settings")) {
 			AccountSettings settings = toAccountSettings(accountObj
 					.getJSONObject("settings"));
 			account.setSettings(settings);
 		}
 
 		JSONArray owners = accountObj.getJSONArray("owners");
 		for (int j = 0; j < owners.length(); j++) {
 			account.addOwner(toUser(owners.getJSONObject(j)));
 		}
 
 		account.setId(id);
 		account.setBalance(balance);
 		account.setType(type);
 		account.setIban(iban);
 		account.setAvailableFunds(availableFunds);
 		account.setOverdraft(overdraft);
 		return account;
 	}
 
 	public static List<Account> toAccountList(JSONArray json)
 			throws JSONException {
 		List<Account> accounts = new ArrayList<Account>();
 
 		for (int i = 0; i < json.length(); i++) {
 			JSONObject accountObj = json.getJSONObject(i);
 			accounts.add(JsonParser.toAccount(accountObj));
 		}
 		return accounts;
 	}
 
 	public static AccountSettings toAccountSettings(
 			JSONObject accountSettingsObj) throws JSONException {
 		String name = accountSettingsObj.has("name") ? accountSettingsObj
 				.getString("name") : null;
 		boolean searchable = accountSettingsObj.has("searchable") ? accountSettingsObj
 				.getBoolean("searchable") : null;
 		AccountSettings settings = new AccountSettings(name, searchable);
 		return settings;
 
 	}
 
 	public static JSONObject toJSON(Transaction transaction) {
 		JSONObject json = new JSONObject();
 		try {
 
 			if (transaction.isFinished() != null) {
 				json.put("finished", transaction.isFinished());
 			}
 			if (transaction.getAmount() != null) {
 				json.put("amount", transaction.getAmount());
 			}
 
 			if (transaction.getReceiverName() != null) {
 				json.put("receiverName", transaction.getReceiverName());
 			}
 
 			if (transaction.getReceiverIban() != null) {
 				json.put("receiverIban", transaction.getReceiverIban());
 			}
 
 			if (transaction.getReceiverReference() != null) {
 				json.put("receiverReference",
 						transaction.getReceiverReference());
 			}
 
 			if (transaction.getCarryOutDate() != null) {
 				json.put("carryOutDate", transaction.getCarryOutDate()
 						.getTime());
 			}
 
 			if (transaction.getPurpose() != null) {
 				json.put("purpose", transaction.getPurpose());
 			}
 
 			if (transaction.getIdentification() != null) {
 				json.put("identification", transaction.getIdentification());
 			}
 
 			return json;
 		} catch (JSONException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public static Transaction toTransaction(JSONObject transactionObj)
 			throws JSONException {
 		TransactionImpl transaction = new TransactionImpl();
 
 		JSONObject ownerObj = transactionObj.getJSONObject("owner");
 		transaction.setAccount(toAccount(ownerObj));
 		transaction.setAmount(transactionObj.getLong("amount"));
 		transaction.setId(transactionObj.getString("id"));
 		transaction.setBill(getString(transactionObj, "bill"));
 		transaction.setCancelled(transactionObj.getBoolean("cancelled"));
 		transaction.setFinished(transactionObj.getBoolean("finished"));
 
 		if (transactionObj.has("carryOutDate")
 				&& !transactionObj.getString("carryOutDate").equals("null")) {
 			transaction.setCarryOutDate(new Date(transactionObj
 					.getLong("carryOutDate")));
 		}
 
 		if (transactionObj.has("createdOn")
 				&& !transactionObj.getString("createdOn").equals("null")) {
 			transaction.setCreatedOn(new Date(transactionObj
 					.getLong("createdOn")));
 		}
 		transaction.setPurpose(getString(transactionObj, "purpose"));
 		transaction
 				.setReceiverIban(getString(transactionObj, ("receiverIban")));
 		transaction
 				.setReceiverName(getString(transactionObj, ("receiverName")));
 		transaction.setReceiverReference(getString(transactionObj,
 				"receiverReference"));
 		transaction.setRecord(getString(transactionObj, "record"));
 		transaction.setSenderIban(getString(transactionObj, "senderIban"));
 		transaction.setSignature(getString(transactionObj, "signature"));
 		String statusString = getString(transactionObj, "status");
 		transaction.setStatus(TransactionStatus.valueOf(statusString));
 		return transaction;
 	}
 
 	public static List<Transaction> toTransactionList(JSONArray transactionArr)
 			throws JSONException {
 		List<Transaction> transactions = new ArrayList<Transaction>();
 
 		for (int i = 0; i < transactionArr.length(); i++) {
 			JSONObject transactionObj = transactionArr.getJSONObject(i);
 			Transaction transaction = toTransaction(transactionObj);
 			transactions.add(transaction);
 		}
 
 		return transactions;
 	}
 
 	public static User toUser(JSONObject json) throws JSONException {
 		User user = new User();
 		user.setId(json.getString("id"));
 		user.setEmail(json.getString("email"));
 		user.setName(json.getString("name"));
 		user.setUserName(json.getString("userName"));
 		return user;
 	}
 
 	private JsonParser() {
 	}
 
 }
