 package net.es.oscars.nsibridge.test.req;
 import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;
 import net.es.oscars.nsibridge.beans.ProvRequest;
 import net.es.oscars.nsibridge.beans.QueryRequest;
 import net.es.oscars.nsibridge.beans.ResvRequest;
 import net.es.oscars.nsibridge.beans.TermRequest;
 import net.es.oscars.nsibridge.beans.config.JettyConfig;
 import net.es.oscars.nsibridge.common.ConfigManager;
 import net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.types.*;
 import net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.headers.CommonHeaderType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.types.TypeValuePairListType;
 import net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.types.TypeValuePairType;
 
 import javax.xml.datatype.DatatypeConfigurationException;
 import javax.xml.datatype.DatatypeFactory;
 import javax.xml.datatype.XMLGregorianCalendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.UUID;
 
 
 public class NSIRequestFactory {
 
     public static QueryRequest getQueryRequest() {
         QueryRequest req = new QueryRequest();
         QueryOperationType op = QueryOperationType.SUMMARY;
         QueryFilterType filter = new QueryFilterType();
         req.setOperation(op);
         req.setQueryFilter(filter);
 
         CommonHeaderType inHeader = makeHeader();
         req.setInHeader(inHeader);
         return req;
     }
 
     public static ProvRequest getProvRequest(ResvRequest resvReq) {
         ProvRequest pq = new ProvRequest();
         pq.setConnectionId(resvReq.getConnectionId());
         CommonHeaderType inHeader = makeHeader();
         pq.setInHeader(inHeader);
         return pq;
     }
 
     public static TermRequest getTermRequest(ProvRequest preq) {
         TermRequest tq = new TermRequest();
         tq.setConnectionId(preq.getConnectionId());
         CommonHeaderType inHeader = makeHeader();
         tq.setInHeader(inHeader);
         return tq;
 
     }
 
     public static ResvRequest getRequest() throws DatatypeConfigurationException {
         Long threeMins = 3 * 60 * 1000L;
         Long tenMins = 10 * 60 * 1000L;
         Date now = new Date();
         Date sDate = new Date();
         sDate.setTime(now.getTime() + threeMins);
         Date eDate = new Date();
         eDate.setTime(sDate.getTime() + tenMins);
 
         ResvRequest req = new ResvRequest();
         ReservationRequestCriteriaType crit = new ReservationRequestCriteriaType();
         PathType pt = new PathType();
         StpType srcStp = new StpType();
         StpType dstStp = new StpType();
         ScheduleType sch = new ScheduleType();
 
         XMLGregorianCalendar sTime = asXMLGregorianCalendar(sDate);
         XMLGregorianCalendar eTime = asXMLGregorianCalendar(eDate);
 
 
         sch.setStartTime(sTime);
         sch.setEndTime(eTime);
 
         srcStp.setOrientation(OrientationType.INGRESS);
         srcStp.setLocalId("urn:ogf:network:stp:esnet.ets:chi-80");
         TypeValuePairType srcTvp = new TypeValuePairType();
         srcTvp.setType("VLAN");
         srcTvp.getValue().add("850");

        TypeValuePairListType slbls = new TypeValuePairListType();
        srcStp.setLabels(slbls);
        slbls.getAttribute().add(srcTvp);
 
         dstStp.setOrientation(OrientationType.EGRESS);
         dstStp.setLocalId("urn:ogf:network:stp:esnet.ets:ps-80");
        TypeValuePairListType dlbls = new TypeValuePairListType();
        dstStp.setLabels(dlbls);
 
         TypeValuePairType dstTvp = new TypeValuePairType();
         dstTvp.setType("VLAN");
         dstTvp.getValue().add("850");
         dstStp.getLabels().getAttribute().add(dstTvp);
 
 
         pt.setDirectionality(DirectionalityType.BIDIRECTIONAL);
         pt.setSourceSTP(srcStp);
         pt.setDestSTP(dstStp);
         pt.setSymmetric(true);
 
         crit.setBandwidth(100);
         crit.setPath(pt);
         crit.setSchedule(sch);
 
 
         String connId = UUID.randomUUID().toString();
         req.setConnectionId(connId);
         req.setDescription("test description");
         req.setGlobalReservationId("some GRI");
         req.setCriteria(crit);
 
 
         CommonHeaderType inHeader = makeHeader();
         req.setInHeader(inHeader);
         return req;
     }
 
     public static CommonHeaderType makeHeader() {
         CommonHeaderType inHeader = new CommonHeaderType();
         inHeader.setProtocolVersion("http://schemas.ogf.org/nsi/2012/03/connection");
         inHeader.setCorrelationId("urn:" + UUID.randomUUID().toString());
         inHeader.setRequesterNSA("urn:ogf:network:nsa:starlight");
         inHeader.setProviderNSA("urn:ogf:network:nsa:esnet");
 
 
 
 
         inHeader.setReplyTo("http://localhost:8088/ConnectionRequester");
         return inHeader;
     }
 
 
     public static XMLGregorianCalendar asXMLGregorianCalendar(java.util.Date date) throws DatatypeConfigurationException {
         DatatypeFactory df = DatatypeFactory.newInstance();
 
         if (date == null) {
             return null;
         } else {
             GregorianCalendar gc = new GregorianCalendar();
             gc.setTimeInMillis(date.getTime());
             return df.newXMLGregorianCalendar(gc);
         }
     }
 
 }
