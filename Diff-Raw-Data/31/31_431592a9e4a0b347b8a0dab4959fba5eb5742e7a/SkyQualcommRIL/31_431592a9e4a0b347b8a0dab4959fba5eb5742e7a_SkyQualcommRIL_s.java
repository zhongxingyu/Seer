 package com.android.internal.telephony;
 
 import android.content.Context;
 import android.os.Message;
 import android.os.Parcel;
 import android.util.Log;
 import static com.android.internal.telephony.RILConstants.*;
 
 public class SkyQualcommRIL extends RIL implements CommandsInterface {
 
     public SkyQualcommRIL(Context context, int networkMode, int cdmaSubscription) {
         super(context, networkMode, cdmaSubscription);
     }
 
     public void
     sendSMS (String smscPDU, String pdu, Message result) {
         RILRequest rr
                 = RILRequest.obtain(RIL_REQUEST_SEND_SMS_EXPECT_MORE, result);
         rr.mp.writeInt(3);
         rr.mp.writeString(smscPDU);
         rr.mp.writeString(pdu);
         rr.mp.writeString("1");
 
         if (RILJ_LOGD) riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
 
         send(rr);
     }
 }
