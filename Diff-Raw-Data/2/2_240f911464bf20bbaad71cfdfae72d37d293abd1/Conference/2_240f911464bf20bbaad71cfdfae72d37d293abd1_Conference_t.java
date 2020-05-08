 package uk.co.brightec.alphaconferences.data;
 
 import org.joda.time.DateTimeZone;
 import org.joda.time.LocalDate;
 import org.json.JSONObject;
 
 import uk.co.brightec.util.JSON;
 import uk.co.brightec.util.ReadablePartialComparator;
 
 
 public class Conference implements Comparable<Conference> {
 
     public final int conferenceId;
     public final String name, text, imageKey, bookingUrl, donationUrl, donationDescription, donationTelephoneNumber;
     public final LocalDate startDate, endDate;
     
     
     Conference(JSONObject o) {
         this.conferenceId = o.optInt("id");
         this.name = JSON.getString(o, "name");
         this.text = JSON.getString(o, "description");
         this.startDate = JSON.getLocalDate(o, "start_date", JSON.DateIntepretation.SECONDS_SINCE_1970, DateTimeZone.UTC);
        this.endDate = JSON.getLocalDate(o, "end_date", JSON.DateIntepretation.SECONDS_SINCE_1970, DateTimeZone.UTC);
         this.imageKey = JSON.getString(o, "imageKey");
         this.bookingUrl = JSON.getString(o, "booking_url");
         this.donationUrl = JSON.getString(o, "donation_url");
         this.donationDescription = JSON.getString(o, "donation_description");
         this.donationTelephoneNumber = JSON.getString(o, "donation_telephone_number");
     }
 
 
     @Override
     public int compareTo(Conference that) {
         return ReadablePartialComparator.NULLS_FIRST.compare(this.startDate, that.startDate);
     }
 
 }
