 package dhbw.stundenplan.google;
 
 import java.io.IOException;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.TimeZone;
 
 import android.accounts.Account;
 import android.app.Activity;
 import android.content.Context;
 import android.database.Cursor;
 
 import com.google.api.client.util.DateTime;
 import com.google.api.services.calendar.Calendar;
 import com.google.api.services.calendar.model.CalendarListEntry;
 import com.google.api.services.calendar.model.Event;
 import com.google.api.services.calendar.model.EventDateTime;
 import com.google.api.services.calendar.model.Events;
 
 import dhbw.stundenplan.database.TerminDBAdapter;
 
 /**
  * 
  * @author DH10HAH
  * 
  */
 public class GoogleKalender
 {
 	Activity activity;
 	Context context;
 
 	public GoogleKalender(Activity activity, Context context)
 	{
 		this.activity = activity;
 		this.context = context;
 	}
 
 	public Boolean ready = false;
 	int test = 0;
 
 	public void ladeTermineInKalender(Account account, String authToken)
 	{
 		ready = false;
 		TerminDBAdapter terminDBAdapter = new TerminDBAdapter(context);
 		Cursor cursor = terminDBAdapter.fetchVorlesungen();
 		if (cursor.moveToFirst())
 		{
 			try
 			{
 				loescheTermine(account, authToken);
 			} catch (IOException e1)
 			{
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 			do
 			{
 				if (cursor.getString(3).length() > 2)
 				{
 					System.out.println(cursor.getString(3));
 					String place = "Raum: " + cursor.getString(4);
 
 					String[] date = cursor.getString(0).split("\\.");
 					String[] startTime = cursor.getString(1).split(":");
 					Date startDate = new Date();
 					/*
 					 * Monat - 1 da sonst falsches ergebnis.
 					 */
 					startDate = new GregorianCalendar(Integer.parseInt(date[2]), Integer.parseInt(date[1]) - 1, Integer.parseInt(date[0]), Integer.parseInt(startTime[0]), Integer.parseInt(startTime[1])).getTime();// (date[2]
 																																																						// +
 																																																						// 1900,
 																																																						// date[1],
 																																																						// date[0]);
 					Date curDate = new GregorianCalendar().getTime();
 					if (startDate.after(curDate))
 					{
 						Date test = new Date(curDate.getTime() + 1209600000 + 1209600000 / 2);// +
 																								// 1209600000
 																								// +
 																								// 1209600000);//
 																								// 4*2
 																								// Wochen
 						if (startDate.before(test))
 						{
 							String[] endTime = cursor.getString(2).split(":");
 
 							Date endDate = new Date();
 							/*
 							 * Monat - 1 da sonst falsches ergebnis.
 							 */
 							endDate = new GregorianCalendar(Integer.parseInt(date[2]), Integer.parseInt(date[1]) - 1, Integer.parseInt(date[0]), Integer.parseInt(endTime[0]), Integer.parseInt(endTime[1])).getTime();// (date[2]
 							try
 							{
 								createTermin(account, authToken, cursor.getString(3), place, startDate, endDate);
 							} catch (IOException e)
 							{
 								// TODO Auto-generated catch
 								// block
 								e.printStackTrace();
 							}
 						}
 					}
 				}
 				System.out.println("+++++++++++++++++++++++++++++++++++++++   " + test + "   ++++++++++++++++++++++++++++++++++++++++++");
 				test++;
 
 			} while (cursor.moveToNext());
 		}
 
 		// System.out.println(authToken);
 		// try
 		// {
 		// createTermin(account, authToken);
 		// } catch (IOException e)
 		// {
 		// // TODO Auto-generated catch block
 		// e.printStackTrace();
 		// System.out.println("ERROR");
 		// }
 		cursor.close();
 		terminDBAdapter.close();
 		ready = true;
 	}
 
 	private void createTermin(Account account, String authToken, String title, String place, Date startDate, Date endDate) throws IOException
 	{
 		Calendar service = CalendarServiceBuilder.build(authToken);// OAuthManager.getInstance().getAuthToken());
 		Event event = new Event();
 
 		event.setSummary("Vorlesung: " + title);
 		event.setLocation(place);
 
 		// ArrayList<EventAttendee> attendees = new ArrayList<EventAttendee>();
 		// attendees.add(new EventAttendee().setEmail("attendeeEmail"));
 		// // ...
 		// event.setAttendees(attendees);
 
 		DateTime start = new DateTime(startDate, TimeZone.getTimeZone("UTC"));
 		event.setStart(new EventDateTime().setDateTime(start));
 		DateTime end = new DateTime(endDate, TimeZone.getTimeZone("UTC"));
 		event.setEnd(new EventDateTime().setDateTime(end));
 
 		Event createdEvent = service.events().insert(gibDHBWKalenderID(), event).execute();
 		System.out.println("1. " + event.getId());
 		System.out.println("2. " + createdEvent.getId());
 	}
 
 	private String gibDHBWKalenderID() throws IOException
 	{
 		Calendar service = CalendarServiceBuilder.build(OAuthManager.getInstance().getAuthToken());
 		/*
 		 * Listet alle Verfgbaren Kalender auf
 		 */
 		com.google.api.services.calendar.model.CalendarList calendarList = service.calendarList().list().execute();
 		while (true)
 		{
 			for (CalendarListEntry calendarListEntry : calendarList.getItems())
 			{
 				System.out.println(calendarListEntry.getSummary());
 				System.out.println(calendarListEntry.getId());
 				if (calendarListEntry.getSummary().contains("DHBW Dualis"))
 				{
 					return calendarListEntry.getId();
 				}
 			}
 			return createCalendar();
 		}
 	}
 
 	private String createCalendar() throws IOException
 	{
 		Calendar service = CalendarServiceBuilder.build(OAuthManager.getInstance().getAuthToken());
 
 		com.google.api.services.calendar.model.Calendar calendar = new com.google.api.services.calendar.model.Calendar();
 
 		calendar.setSummary("DHBW Dualis");
 		calendar.setTimeZone("Europe/Berlin");
 
 		com.google.api.services.calendar.model.Calendar createdCalendar = service.calendars().insert(calendar).execute();
 
 		System.out.println(createdCalendar.getId());
 		return createdCalendar.getId();
 	}
 
 	private void loescheTermine(Account account, String authToken) throws IOException
 	{
 		String dhbwKalenderID = gibDHBWKalenderID();
 		Calendar service = CalendarServiceBuilder.build(OAuthManager.getInstance().getAuthToken());
 		Events events = service.events().list(dhbwKalenderID).execute();
 		Date curDate = new GregorianCalendar().getTime();
 		if (events.getItems() != null)
 		{
			while (true)
 			{
				for (Event event : events.getItems())
 				{
					// TODO: Datumsbereich festlegen.

					System.out.println(event.getSummary());
					if (curDate.before(new Date(event.getStart().getDateTime().getValue())))
 					{
						if (event.getSummary().contains("Vorlesung"))
						{
							service.events().delete(dhbwKalenderID, event.getId()).execute();
						}
 					}
 				}
 			}
 		}
 	}
 }
