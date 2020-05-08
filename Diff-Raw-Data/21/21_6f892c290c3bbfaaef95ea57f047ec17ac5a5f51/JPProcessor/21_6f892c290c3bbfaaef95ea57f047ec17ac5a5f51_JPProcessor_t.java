 /*******************************************************************************
  * Copyright 2012-2013 Trento RISE
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *        http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either   express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 
 package eu.trentorise.smartcampus.communicator.renderers;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Locale;
 import java.util.Map;
 
 import android.content.Context;
 import eu.trentorise.smartcampus.communicator.R;
 import eu.trentorise.smartcampus.communicator.custom.NotificationProcessor;
 import eu.trentorise.smartcampus.communicator.model.Notification;
 
 /**
  * @author raman
  *
  */
 public class JPProcessor implements NotificationProcessor {
 	private static final DateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
 	private static final int AGENCYID_BUS = 12;
 	private static final int AGENCYID_TRAIN_TM = 10;
 	private static final int AGENCYID_TRAIN_BZVR = 5;
 	private static final int AGENCYID_TRAIN_TNBDG = 6;
 
 	@Override
 	public void processMessage(Context ctx, Notification notification) {
		if (notification.getContent() == null || notification.getContent().isEmpty()) {
 			return;
 		}
 
 		Map<String, Object> content = notification.getContent();
 		String journeyName = notification.getTitle();
 		Integer agencyId = Integer.parseInt((String) content.get("agencyId"));
 		Integer delay = (Integer) content.get("delay"); // milliseconds
 		String line = "?";
 		if (content.get("routeShortName") != null) {
 			line = (String) content.get("routeShortName");
 		} else if (content.get("routeId") != null) {
 			line = (String) content.get("routeId");
 		}
 		String tripId = (String) content.get("tripId");
 		String direction = (String) content.get("direction");
 		Long originalFromTime = (Long) content.get("from"); // milliseconds
 		String stopName = (String) content.get("station");
 
 		// title
 		if (journeyName != null && journeyName.length() != 0) {
 			notification.setTitle(ctx.getString(R.string.notifications_itinerary_delay_title, journeyName));
 		}
 
 		// description
 		StringBuilder description = new StringBuilder();
 
 		// delay
 		if (delay != null && delay > 0) {
 			int minutes = delay / 60000;
 			if (minutes == 1) {
 				description.append(ctx.getString(R.string.notifications_itinerary_delay_min, minutes));
 			} else {
 				description.append(ctx.getString(R.string.notifications_itinerary_delay_mins, minutes));
 			}
 		}
 
 		// line/train (with train number) and direction
 		if (line != null && line.length() > 0 && direction != null && direction.length() > 0) {
 			description.append("\n");
 			if (agencyId == AGENCYID_BUS) {
 				description.append(ctx.getString(R.string.notifications_itinerary_delay_bus, line, direction));
 			} else if (agencyId == AGENCYID_TRAIN_TM || agencyId == AGENCYID_TRAIN_BZVR || agencyId == AGENCYID_TRAIN_TNBDG) {
 				String train = line;
 				if (tripId != null) {
 					train += " " + tripId;
 				}
 				description.append(ctx.getString(R.string.notifications_itinerary_delay_train, train, direction));
 			}
 		}
 
 		// original data
 		if (originalFromTime != null && stopName != null) {
 			Calendar origCal = Calendar.getInstance();
 			origCal.setTimeInMillis(originalFromTime);
 			String originalFromTimeString = timeFormat.format(origCal.getTime());
 			description.append("\n");
 			description.append(ctx.getString(R.string.notifications_itinerary_delay_original_schedule,
 					originalFromTimeString, stopName));
 		}
 
 		notification.setDescription(description.toString());
 
 	}
 
 }
