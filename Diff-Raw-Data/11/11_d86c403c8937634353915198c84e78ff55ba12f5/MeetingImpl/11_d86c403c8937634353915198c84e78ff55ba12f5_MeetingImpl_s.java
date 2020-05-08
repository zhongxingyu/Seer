 import java.io.Serializable;
 import java.util.*;
 
 public class MeetingImpl implements Meeting, Serializable
 {
     private int meetingId;
     private Set<Contact> contactsAtMeeting = new HashSet<Contact>();
     private Calendar meetingCal;
     private boolean past = false;
     private boolean future = false;
     private String meetingNotes = "";
 
     public MeetingImpl(int id, Set<Contact> set, Calendar date)
     {
         this.meetingId = id;
         this.contactsAtMeeting.addAll(set);
         this.meetingCal = date;
 
         Calendar currentDate = GregorianCalendar.getInstance();
         if (currentDate.after(date))       // i.e if meeting date is in the past
         {
             this.past = true;
         }
         else if (currentDate.before(date))       // i.e. if meeting date is in the future
         {
             this.future = true;
         }
     }
 
     public MeetingImpl()
     {
         // no args constructor for comparator
     }
 
     public int getId()
     {
         return this.meetingId;
     }
 
     public Calendar getDate()
     {
         return this.meetingCal;
     }
 
     public String getNotes()
     {
         return this.meetingNotes;
     }
 
     public Set<Contact> getContacts()
     {
         return this.contactsAtMeeting;
     }
 
     public String getSetInfo()
     {
         String setInfo = "";
         for (Iterator<Contact> itr = this.contactsAtMeeting.iterator(); itr.hasNext();)
         {
             ContactImpl tmp = (ContactImpl) itr.next();
             setInfo += tmp.getInfo() + "\n";
         }
        /** @return removes final newline character, which is
         * unnecessary as there are no more contacts to follow) */
         return setInfo.substring(0, (setInfo.length() - 1));
     }
 
     public String getMeetingInfo()
     {
         String id = "Meeting Id: " + this.meetingId;
         String contacts = "Contacts at Meeting: " + this.getSetInfo();
         String date = "Date of Meeting: " + this.meetingCal.get(GregorianCalendar.DAY_OF_MONTH) + "/" +
                 (this.meetingCal.get(GregorianCalendar.MONTH) + 1) + "/" + this.meetingCal.get(GregorianCalendar.YEAR);
         String notes = "Meeting Notes: " + this.getNotes();
         return (id + "\n" + contacts + "\n" + date + "\n" + notes);
     }
 
     public String getFormattedDate()
     {
         return "Date of Meeting: " + this.meetingCal.get(GregorianCalendar.DAY_OF_MONTH) + "/" +
                 (this.meetingCal.get(GregorianCalendar.MONTH) + 1) + "/" + this.meetingCal.get(GregorianCalendar.YEAR);
     }
 
     public boolean inPast()
     {
         return past;
     }
 
     public boolean inFuture()
     {
         return future;
     }
 
     public void addNotes(String note)
     {
         /** @return prints a newline at the end of each added note and a dash
         at the start so the list of notes remains clear to read */
         meetingNotes += ("-" + note + "\n");
     }
 
 
     /** @param whatKindOfMeeting - flag passed from ContactManager so we know
      * whether getFutureMeeting(), getPastMeeting() or getMeeting() has been called */
     protected static Meeting returnMeeting(Set<Meeting> meetingSet, int id, char whatKindOfMeeting)
     {
         for (Meeting meeting : meetingSet)
         {
             if (meeting.getId() == id && whatKindOfMeeting == 'f')   // i.e. this needs to be a FUTURE meeting
             {
                 if (((MeetingImpl)meeting).inFuture() == true)     // use boolean getter to confirm this is a FUTURE meeting
                 {
                     /** if this condition true we have found id AND confirmed the meeting to be FUTURE; @return itr.next */
                     return meeting;
                 }
                 else if (((MeetingImpl)meeting).inPast() == true)       // i.e. if this is a PAST meeting [error]
                 {
                     /** if this condition true we have found id BUT the meeting is PAST; @throws IllegalArgsException */
                     throw new IllegalArgumentException("Meeting with specified ID happened on " + ((MeetingImpl)meeting).getFormattedDate());
                 }
             }
             else if (meeting.getId() == id && whatKindOfMeeting == 'p')   // i.e. this needs to be a PAST meeting
             {
                 if (((MeetingImpl)meeting).inPast() == true)   // use boolean getter to confirm this is a PAST meeting
                 {
                     /** if this condition true we have found id AND confirmed the meeting to be PAST; @return itr.next */
                     return meeting;
                 }
                 else if (((MeetingImpl)meeting).inFuture() == true)    // i.e. if this is a FUTURE meeting [error]
                 {
                     /** if this condition true we have found id BUT the meeting is FUTURE; @throws IllegalArgsException */
                     throw new IllegalArgumentException("Meeting with specified ID will not happen until " + ((MeetingImpl)meeting).getFormattedDate());
                 }
             }
             else if (meeting.getId() == id && whatKindOfMeeting == 'm')   // i.e. this needs to be just a MEETING [getMeeting]
             {
                 /** can just return; no need to check if meeting past or future as it can be both to satisfy getMeeting() */
                 return meeting;
             }
            else // if the id is never found at all
            {
                System.err.println("No meeting found with id " + id);
                return null;
            }
         }
        System.err.println("Unable to read list of meetings. Please ensure it has readable permissions and/or has been created");
         return null;
     }
 
     public static Comparator<Meeting> MeetingComparator = new Comparator<Meeting>()
     {
         @Override
         public int compare(Meeting m1, Meeting m2)
         {
             Calendar cal1 = m1.getDate();      // the calendar for the first meeting
             Calendar cal2 = m2.getDate();   // the calendar for the second meeting
             long cal1Time = cal1.getTimeInMillis() ;
             long cal2Time = cal2.getTimeInMillis();
             /** @return a number which will unambiguously place each calendar in order (using milliseconds)
              * 1 if cal1Time is greater than cal2Time, -1 for vice-versa and 0 for equality*/
             return (cal1Time > cal2Time) ? 1 : (cal1Time < cal2Time) ? -1 : 0;        // used ternary operator to save space
         }
     };
 }
