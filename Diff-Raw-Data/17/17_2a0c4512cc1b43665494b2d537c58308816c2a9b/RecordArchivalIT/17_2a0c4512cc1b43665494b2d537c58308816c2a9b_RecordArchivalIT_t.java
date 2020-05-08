 package com.williamsinteractive.casino.wager.procedures;
 
 import org.junit.Test;
 import org.voltdb.VoltTable;
 import org.voltdb.VoltType;
 import org.voltdb.client.ClientResponse;
 
 import static org.hamcrest.CoreMatchers.containsString;
 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.CoreMatchers.not;
 import static org.hamcrest.CoreMatchers.notNullValue;
 import static org.junit.Assert.assertThat;
 
 /**
  * TODO: document!
  *
  * @author Petter Måhlén
  */
 public class RecordArchivalIT extends VoltDbTestSupport {
     @Test
     public void shouldSetArchivalTimestampNormally() throws Exception {
         prepareWagerRound();
        prepareConfirmedOutcome();
 
         ClientResponse response  = recordArchival();
 
         assertThat(response.getAppStatus(), equalTo(RecordArchival.SUCCESS));
 
         VoltTable wagerRound = readAllData("WAGER_ROUND_SELECT_ALL");
         assertThat(wagerRound.getRowCount(), equalTo(1));
 
         wagerRound.advanceRow();
         assertThat(wagerRound.get("archive_timestamp", VoltType.TIMESTAMP), is(notNullValue()));
     }
 
     @Test
     public void shouldFailIfNoWager() throws Exception {
         ClientResponse response  = recordArchival();
 
         assertThat(response.getAppStatus(), equalTo(RecordArchival.NO_WAGER));
         assertThat(response.getAppStatusString(), containsString("No wager found"));
         assertThat(response.getAppStatusString(), containsString(String.valueOf(WAGER_ROUND_ID)));
     }
 
     @Test
    public void shouldFailIfNoConfirmedOutcome() throws Exception {
         prepareWagerRound();
         prepareWager();
        prepareOutcome();
 
         ClientResponse response  = recordArchival();
 
         assertThat(response.getAppStatus(), equalTo(RecordArchival.NO_OUTCOME));
         assertThat(response.getAppStatusString(), containsString("No outcome found"));
         assertThat(response.getAppStatusString(), containsString(String.valueOf(WAGER_ROUND_ID)));
     }
 
     @Test
     public void shouldFailIfAlreadyArchived() throws Exception {
         prepareWagerRound();
         prepareWager();
        prepareConfirmedOutcome();
         prepareArchival();
 
         ClientResponse response  = recordArchival();
 
         assertThat(response.getAppStatus(), equalTo(RecordArchival.ALREADY_ARCHIVED));
         assertThat(response.getAppStatusString(), containsString("has already been archived"));
         assertThat(response.getAppStatusString(), containsString(String.valueOf(WAGER_ROUND_ID)));
     }
 
     private ClientResponse recordArchival() throws Exception {
         ClientResponse response = client.callProcedure("RecordArchival", WAGER_ROUND_ID);
 
         assertThat(response, isSuccess());
         return response;
     }
 }
