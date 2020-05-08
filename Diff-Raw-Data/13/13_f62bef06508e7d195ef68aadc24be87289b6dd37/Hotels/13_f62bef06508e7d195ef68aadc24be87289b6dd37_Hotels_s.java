 package controllers;
 
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import javax.xml.datatype.DatatypeConfigurationException;
 import javax.xml.datatype.DatatypeFactory;
 import javax.xml.datatype.XMLGregorianCalendar;
 
 import models.Room;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 
 import play.Logger;
 import play.cache.CacheFor;
 import play.libs.WS;
 import play.libs.XPath;
 import webserbice.GetHotelInfoListResponse.GetHotelInfoListResult;
 import webserbice.RateQueryWebResponse.RateQueryWebResult;
 import webserbice.WsDataEngineForWebSite;
 import webserbice.WsDataEngineForWebSiteSoap;
 
 public class Hotels extends Application {
     @CacheFor("2min")
     public static void search() {
         params.put("from", "2011-09-27");
         params.put("to", "2011-09-30");
         Date from = params.get("from", Date.class);
         Date to = params.get("to", Date.class);
         Logger.info(from.toString() + " to " + to.toString());
         // gets(from, to);
         renderArgs.put("hotels", getHotels(from, to));
         index();
     }
 
     private static Document gets3(Date from, Date to) {
         String wsReq = "<?xml version=\"1.0\" encoding=\"utf-8\"?><soap12:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap12=\"http://www.w3.org/2003/05/soap-envelope\"><soap12:Body><RateQueryWeb xmlns=\"http://tempuri.org/\"><city></city><hotelId></hotelId><brand></brand><roomType></roomType><arriveTime></arriveTime><departureTime></departureTime><guestNumber>1</guestNumber><roomNumber>1</roomNumber><money>0</money><money1>0</money1><rateCode>0</rateCode><hGuest>0</hGuest><hCompany>0</hCompany><hAgency>0</hAgency><hRoomCenter>0</hRoomCenter><mode>0</mode><closed>0</closed><src>0</src></RateQueryWeb></soap12:Body></soap12:Envelope>";
         // String wsReq = "<?xml version=\"1.0\" encoding=\"utf-8\"?><soap12:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap12=\"http://www.w3.org/2003/05/soap-envelope\"><soap12:Body><RateQueryWeb xmlns=\"http://tempuri.org/\"><city></city><hotelId></hotelId><brand></brand><roomType></roomType><arriveTime>2011-09-28</arriveTime><departureTime>2011-09-30</departureTime><guestNumber>1</guestNumber><roomNumber>1</roomNumber><money>0</money><money1>0</money1><rateCode>0</rateCode><hGuest>0</hGuest><hCompany>0</hCompany><hAgency>0</hAgency><hRoomCenter>0</hRoomCenter><mode>0</mode><closed>0</closed><src>0</src></RateQueryWeb></soap12:Body></soap12:Envelope>";
         // String wsReq = "<?xmlversion=\"1.0\"encoding=\"utf-8\"?><soap:Envelopexmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"><soap:Body><RateQueryWebxmlns=\"http://tempuri.org/\"><city></city><hotelId></hotelId><brand></brand><roomType></roomType><arriveTime>2011-09-27</arriveTime><departureTime>2011-09-30</departureTime><guestNumber>1</guestNumber><roomNumber>1</roomNumber><money>0</money><money1>0</money1><rateCode>0</rateCode><hGuest>0</hGuest><hCompany>0</hCompany><hAgency>0</hAgency><hRoomCenter>0</hRoomCenter><mode>0</mode><closed>0</closed><src>0</src></RateQueryWeb></soap:Body></soap:Envelope>";
         // String wsReq = "<?xmlversion=\"1.0\"encoding=\"utf-8\"?><soap12:Envelopexmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"xmlns:soap12=\"http://www.w3.org/2003/05/soap-envelope\"><soap12:Header><WSAuthHeaderxmlns=\"http://tempuri.org/\"><UserName>string</UserName><Password>string</Password></WSAuthHeader></soap12:Header><soap12:Body><GetHotelInfoListxmlns=\"http://tempuri.org/\"><hotelid></hotelid><city></city><brand></brand></GetHotelInfoList></soap12:Body></soap12:Envelope>";
         Document doc = WS.url("http://218.14.215.13:8080/DataEngine/wsDataEngineForWebSite.asmx?wsdl").setHeader("content-type", "application/soap+xml; charset=utf-8").body(wsReq).post().getXml();
        System.out.println(doc + "===========================");
         /*String mobileCode = "13826526941";
         String wsReq = "<?xml version=\"1.0\" encoding=\"utf-8\"?><soap12:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap12=\"http://www.w3.org/2003/05/soap-envelope\"><soap12:Body><getMobileCodeInfo xmlns=\"http://WebXml.com.cn/\"><mobileCode>"+mobileCode+"</mobileCode><userID></userID></getMobileCodeInfo></soap12:Body></soap12:Envelope>";
         Document doc = WS.url("http://webservice.webxml.com.cn/WebServices/MobileCodeWS.asmx?wsdl").setHeader("content-type", "application/soap+xml").body(wsReq).post().getXml();
         String result = doc.getElementsByTagName("getMobileCodeInfoResult").item(0).getTextContent();
         System.out.println(doc+"==========================="+result);*/
         return doc;
     }
 
     private static List<Room> getHotels(Date from, Date to) {
         List<Room> hotels = new ArrayList<Room>();
         RateQueryWebResult rateQueryWebResult = gs("JHH003", from, to);
         Element element = (Element) rateQueryWebResult.getAny();
         for (Node node : XPath.selectNodes("//NewDataSet//Table1", element)) {
             String hotelId = XPath.selectText("hotelid", node);
             String name = XPath.selectText("descript", node);
             String code = XPath.selectText("code", node);
             String rate = XPath.selectText("rate", node);
             String rateCode = XPath.selectText("ratecode", node);
             Room hotel = new Room();
             hotel.setHotelId(hotelId);
             hotel.setRoomName(name);
             hotel.setCode(code);
             hotel.setRate(rate);
             hotel.setRateCode(rateCode);
             hotels.add(hotel);
         }
         return hotels;
     }
 
     private static RateQueryWebResult gs(String hotelId, Date from, Date to) {
         WsDataEngineForWebSite ws = new WsDataEngineForWebSite();
         WsDataEngineForWebSiteSoap wsp = ws.getWsDataEngineForWebSiteSoap12();
         RateQueryWebResult rateQueryWebResult = wsp.rateQueryWeb("", hotelId, "", "", convertToXMLGregorianCalendar(from), convertToXMLGregorianCalendar(to), 1, 1, new BigDecimal(0), new BigDecimal(0), "", "", "", "", "", "", "", "");
         return rateQueryWebResult;
     }
 
     private static Element initHotelInfoList() {
         Logger.info("getHotelInfoList远程服务获取数据");
         WsDataEngineForWebSite ws = new WsDataEngineForWebSite();
         WsDataEngineForWebSiteSoap wsp = ws.getWsDataEngineForWebSiteSoap12();
         GetHotelInfoListResult hotelInfoListResult = wsp.getHotelInfoList("", "", "");
         Element hotelInfoListResultElement = (Element) hotelInfoListResult.getAny();
         for (Node event : XPath.selectNodes("//NewDataSet//Table1", hotelInfoListResultElement)) {
             String hotelid = XPath.selectText("hotelid", event);
             String descript = XPath.selectText("descript", event);
         }
         return hotelInfoListResultElement;
     }
 
     private static XMLGregorianCalendar convertToXMLGregorianCalendar(Date date) {
         GregorianCalendar cal = new GregorianCalendar();
         cal.setTime(date);
         XMLGregorianCalendar gc = null;
         try {
             gc = DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
         } catch (DatatypeConfigurationException e) {
             Logger.error(e.getMessage());
         }
         return gc;
     }
 }
