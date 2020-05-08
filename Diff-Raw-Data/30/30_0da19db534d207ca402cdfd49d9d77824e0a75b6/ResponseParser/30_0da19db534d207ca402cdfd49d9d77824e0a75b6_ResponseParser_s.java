 package com.g6pay.util;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 
 import com.g6pay.dto.OfferDTO;
 import com.g6pay.dto.TransactionDTO;
 
 /**
  * G6 Response parsing.
  * 
  * Note: There is a hacky, specific JSON implementation to avoid bloating SDK 
  * download size.  Use this class at your own risk. Seriously.
  * 
  * @author tooembarrassedtosay
  * 
  */
 public class ResponseParser {
 
     private static String sdfs = "yyyy-MM-dd' 'HH:mm:ss";
     
     private static SimpleDateFormat getSdf() {
         return new SimpleDateFormat(sdfs);
     }
 
     private static Date parseDate(String dateString) {
         try {
             return getSdf().parse(dateString);
         } catch(Exception e) {
             return null;
         }
     }
     /*
      * [ { "user_id":"example@test.com", "offer_id":6,
      * "offer_name":"Mozizzo for iPhone", "net_payout":"5.76",
      * "virtual_currency_amount":"19.58", "date":"2010-10-17 09:40:15",
      * "description":"offer completion" }, { "user_id":"testUser", "offer_id":3,
      * "offer_name":"Livelinks", "net_payout":"10.00",
      * "virtual_currency_amount":"4.34", "date":"2010-10-05 02:26:43",
      * "description":"" }, ]
      */
     private static final String DTO_USER_ID = "user_id";
     private static final String DTO_OFFER_ID = "offer_id";
     private static final String DTO_OFFER_NAME = "offer_name";
     private static final String DTO_NET_PAYOUT = "net_payout";
     private static final String DTO_VIR_CUR_AMT = "virtual_currency_amount";
     private static final String DTO_DATE = "date";
     private static final String DTO_DESC = "description";
     private static final String DTO_USER_BALANCE = "user_balance";
     private static final String DTO_SIGNATURE = "signature";
 
     // net_payout=1.23:virtual_currency_amount=3.45:offer_id=334:user_id=user@example.com:user_balance=34.56signature=a6dd4bd06fbf2b69e3d258c3a5a70bfd2ea47dd1b704d48693dd4e43bdd09e7
     public static OfferDTO offerFromMap(HashMap<String, String> map) {
         OfferDTO dto = new OfferDTO();
         
         String key;
 
         key = DTO_USER_ID;
         if (map.get(key) != null) {
             dto.setUserId(map.get(key));
         }
 
         key = DTO_OFFER_ID;
         if (map.get(key) != null) {
             dto.setOfferId(map.get(key));
         }
 
         key = DTO_OFFER_NAME;
         if (map.get(key) != null) {
             dto.setOfferName(map.get(key));
         }
 
         key = DTO_NET_PAYOUT;
         if (map.get(key) != null) {
             try {
                 float amount = Float.parseFloat(map.get(key));
                 dto.setNetPayout(amount);
             } catch (Exception ex) {
             }
         }
 
         key = DTO_VIR_CUR_AMT;
         if (map.get(key) != null) {
             try {
                 float amount = Float.parseFloat(map.get(key));
                 dto.setVirtualCurrencyAmount(amount);
             } catch (Exception ex) {
             }
         }
 
         key = DTO_SIGNATURE;
         if (map.get(key) != null) {
             dto.setSignature(map.get(key));
         }
 
         key = DTO_USER_BALANCE;
         if (map.get(key) != null) {
             try {
                 float amount = Float.parseFloat(map.get(key));
                 dto.setUserBalance(amount);
             } catch (Exception ex) {
             }
         }
 
 
         return dto;    }
 
     public static TransactionDTO transactionFromMap(HashMap<String, String> map) {
 
         TransactionDTO dto = new TransactionDTO();
 
         String key;
 
         key = DTO_USER_ID;
         if (map.get(key) != null) {
             dto.setUserId(map.get(key));
         }
 
         key = DTO_OFFER_ID;
         if (map.get(key) != null) {
             dto.setOfferId(map.get(key));
         }
 
         key = DTO_OFFER_NAME;
         if (map.get(key) != null) {
             dto.setOfferName(map.get(key));
         }
 
         key = DTO_NET_PAYOUT;
         if (map.get(key) != null) {
             try {
                 float amount = Float.parseFloat(map.get(key));
                 dto.setNetPayout(amount);
             } catch (Exception ex) {
             }
         }
 
         key = DTO_VIR_CUR_AMT;
         if (map.get(key) != null) {
             try {
                 float amount = Float.parseFloat(map.get(key));
                 dto.setVirtualCurrencyAmount(amount);
             } catch (Exception ex) {
             }
         }
 
         key = DTO_DATE;
         if (map.get(key) != null) {
             dto.setDate(parseDate(map.get(key)));
         }
 
         key = DTO_DESC;
         if (map.get(key) != null) {
             dto.setDescription(map.get(key));
         }
 
         return dto;
     }
 
     public static ArrayList<TransactionDTO> parseTransactions(String response) {
 
         try {
             ArrayList<TransactionDTO> result = new ArrayList<TransactionDTO>();
 
             response = response.trim();
             String listOfDicts = response.substring(1, response.length());
 
             StringBuffer keyBuf = new StringBuffer();
             StringBuffer valBuf = new StringBuffer();
 
             boolean valueIsNum = false;
             String key = null;
             String val = null;
             int state = 0;
             int oldState = state;
             // 0 = new dto
             // 1 = pre-key
             // 2 = key
             // 3 = between key/val
             // 4 = pre-val
             // 5 = val
             // 6 = finished val
             char oldC = '0';
             HashMap<String, String> dict = new HashMap<String, String>();
 
             for (int i = 0; i < listOfDicts.length(); i++) {
                 char c = listOfDicts.charAt(i);
 
                 if (oldState != state) {
                     // Log.d("G6Pay", "State changed to " + state);
                 }
                 oldState = state;
 
                 switch (state) {
                 case 0: // new DTO
                     // reset everything
                     valueIsNum = false;
                     keyBuf = new StringBuffer();
                     valBuf = new StringBuffer();
                     key = null;
                     val = null;
                     dict = new HashMap<String, String>();
 
                     if (c == '{')
                         state++;
                     break;
                 case 1: // pre-key
                     if (c == '"')
                         state++;
                     break;
                 case 2: // key
                     if (c == '"') {
                         state++;
                         key = keyBuf.toString();
                     } else {
                         keyBuf.append(c);
                     }
                     break;
                 case 3: // between key and value
                     if (c == ':')
                         state++;
                     break;
                 case 4: // pre-val
                     if (c >= '0' && c <= '9' || c == '-' || c == '.') {
                         valueIsNum = true;
                         valBuf.append(c);
                         state++;
                     }
                     if (c == '"') {
                         valueIsNum = false;
                         state++;
                     }
                     break;
                 case 5: // val
                     boolean nextState = false;
                     if (valueIsNum) {
                         if (c >= '0' && c <= '9' || c == '-' || c == '.') {
                             valBuf.append(c);
                         } else {
                             // we decrement, because we're on a char we
                             // don't understand
                             i--;
                             nextState = true;
                         }
                     } else {
                         if (c == '"' && oldC != '\\') {
                             nextState = true;
                         } else {
                             valBuf.append(c);
                         }
                     }
                     if (nextState) {
                        val = valBuf.toString();
                        dict.put(key.trim(), val.trim());
                         state++;
                         keyBuf = new StringBuffer();
                         valBuf = new StringBuffer();
                     }
                     break;
                 case 6: // post-val
                     if (c == ',')
                         state = 1; // pre-key
                     if (c == '}') {
                         // we've wrapped up another DTO, phew
                         result.add(transactionFromMap(dict));
                         state = 0; // pre-dto
                     }
                     break;
                 default:
                     break;
                 }
                 oldC = c;
             }
             
             return result;
         } catch (Exception ex) {
             return null;
         }
 
     }
 
     /**
      * net_payout=1.23:virtual_currency_amount=3.45:offer_id=334:user_id=user@example.com:user_balance=34.56signature=a6dd4bd06fbf2b69e3d258c3a5a70bfd2ea47dd1b704d48693dd4e43bdd09e7c
      * Returns null if no offer was completed
      * @param body
      * @return
      */
     public static OfferDTO parseOffer(String body) {
         
         try {
             
             HashMap<String, String> dict = new HashMap<String, String>();
             
             String[] tokens = body.trim().split(":");
             for (String keyVal : tokens) {
                 String[] keyValList = keyVal.split("=", 2);
                 
                 String key = keyValList[0];
                 String val = keyValList[1];
                 
                 dict.put(key, val);
             }
             OfferDTO offer = offerFromMap(dict);
             
             if (offer.getOfferId() == null)
                 return null;
             
             return offer;
         } catch (Exception ex) {
             ex.printStackTrace();
             return null;
         }
     }
 
     public static float balanceFromResponse(String body) throws Exception {
         
         try {
             String[] tokens = body.trim().split(":", 2);
             
             String balance = tokens[1].trim();
             if (balance.startsWith("\""))
                 balance = balance.substring(1, balance.length()-1);
             return Float.parseFloat(balance);
         } catch (Exception ex) {
             throw new Exception();
         }
     }
 
 }
