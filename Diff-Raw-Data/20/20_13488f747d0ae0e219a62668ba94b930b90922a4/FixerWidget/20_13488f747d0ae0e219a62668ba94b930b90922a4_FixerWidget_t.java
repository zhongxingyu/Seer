 /*	    Wifi Fixer for Android
     Copyright (C) 2010-2013  David Van de Ven
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see http://www.gnu.org/licenses
  */
 
 package org.wahtod.wififixer.widget;
 
 import org.wahtod.wififixer.prefs.PrefConstants;
 import org.wahtod.wififixer.prefs.PrefUtil;
 import org.wahtod.wififixer.utility.StatusDispatcher;
 
 import android.appwidget.AppWidgetManager;
 import android.appwidget.AppWidgetProvider;
 import android.content.Context;
 import android.content.Intent;
 
 public class FixerWidget extends AppWidgetProvider {
 	public static final String W_INTENT = "org.wahtod.wififixer.WIDGET";
 
 	@Override
 	public void onDisabled(Context context) {
 		super.onDisabled(context);
 		PrefUtil.writeBoolean(context, PrefConstants.HAS_WIDGET, false);
 		PrefUtil.notifyPrefChange(context, PrefConstants.HAS_WIDGET, false);
 	}
 
 	@Override
 	public void onEnabled(Context context) {
		PrefUtil.writeBoolean(context, PrefConstants.HAS_WIDGET, true);
 		PrefUtil.notifyPrefChange(context, PrefConstants.HAS_WIDGET, true);
 		super.onEnabled(context);
 	}
 
 	@Override
 	public void onReceive(Context context, Intent intent) {
 		if (intent.getAction().equals(
 				StatusDispatcher.ACTION_WIDGET_NOTIFICATION))
 			doStatusUpdate(context, intent);
 		super.onReceive(context, intent);
 	}
 
 	private void doStatusUpdate(final Context context, Intent intent) {
 		Intent start = UpdateService.updateIntent(context,
 				StatusUpdateService.class, FixerWidget.class.getName());
 		start.fillIn(intent, Intent.FILL_IN_DATA);
 		context.startService(start);
 	}
 
 	@Override
 	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
 			int[] appWidgetIds) {
 		/*
 		 * Send Update To Widgets
 		 */
 		context.startService(UpdateService.updateIntent(context,
 				UpdateService.class, FixerWidget.class.getName()));
 		super.onUpdate(context, appWidgetManager, appWidgetIds);
 	}
 };
