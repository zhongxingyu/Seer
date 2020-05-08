 package de.urlaubr.ws.client;
 
 import de.urlaubr.ws.TravelService;
 import de.urlaubr.ws.domain.Booking;
 import de.urlaubr.ws.domain.Customer;
 import de.urlaubr.ws.domain.SearchParams;
 import de.urlaubr.ws.domain.Traveler;
 import de.urlaubr.ws.domain.Vacation;
 import org.apache.axiom.om.OMElement;
 import org.apache.axis2.AxisFault;
 import org.apache.axis2.addressing.EndpointReference;
 import org.apache.axis2.client.Options;
 import org.apache.axis2.client.ServiceClient;
 import org.apache.axis2.databinding.utils.BeanUtil;
 import org.apache.axis2.engine.DefaultObjectSupplier;
 
 import javax.xml.namespace.QName;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 public class TravelClient implements TravelService {
 
     public static final String WS_ADDRESS = "http://localhost:8080/axis2/services/fhdw-travelservice-ws-1.0";
     public static final String NAMESPACE_URI = "http://ws.urlaubr.de";
     private ServiceClient sender;
 
     public TravelClient() {
         try {
             sender = new ServiceClient();
             Options options = sender.getOptions();
             EndpointReference targetEPR = new EndpointReference(WS_ADDRESS);
             options.setTo(targetEPR);
         }
         catch (AxisFault e) {
             e.printStackTrace();
         }
     }
 
     @Override
     public Integer login(String username, String password) {
         QName opFindHotel = new QName(NAMESPACE_URI, "login");
         Object[] opArgs = new Object[]{username, password};
         OMElement request = BeanUtil.getOMElement(opFindHotel, opArgs, null, false, null);
         try {
             OMElement response = sender.sendReceive(request);
             Object[] result = BeanUtil.deserialize(response, new Class[]{Integer.class}, new DefaultObjectSupplier());
             if (result.length == 1) {
                 return (Integer) result[0];
             }
         }
         catch (AxisFault e) {
             e.printStackTrace();
         }
         return null;
     }
 
     @Override
     public void logout(Integer sessionKey) {
         QName opFindHotel = new QName(NAMESPACE_URI, "logout");
         Object[] opArgs = new Object[]{sessionKey};
         OMElement request = BeanUtil.getOMElement(opFindHotel, opArgs, null, false, null);
         try {
             sender.sendReceive(request);
         }
         catch (AxisFault e) {
             e.printStackTrace();
         }
     }
 
     @Override
     public List<Vacation> getTopseller() {
         QName opFindHotel = new QName(NAMESPACE_URI, "getTopseller");
         Object[] opArgs = new Object[]{};
         OMElement request = BeanUtil.getOMElement(opFindHotel, opArgs, null, false, null);
         try {
             OMElement response = sender.sendReceive(request);
             Object[] result = BeanUtil.deserialize(response, new Class[]{Vacation.class}, new DefaultObjectSupplier());
             List<Vacation> vacations = new ArrayList<Vacation>();
             for (Object element : result) {
                 vacations.add((Vacation) element);
             }
             return vacations;
         }
         catch (AxisFault e) {
             e.printStackTrace();
         }
         return null;
     }
 
     @Override
     public List<Booking> getMyVacations(Integer sessionKey) throws AxisFault {
         QName opFindHotel = new QName(NAMESPACE_URI, "getMyVacations");
         Object[] opArgs = new Object[]{sessionKey};
         OMElement request = BeanUtil.getOMElement(opFindHotel, opArgs, null, false, null);
         try {
             OMElement response = sender.sendReceive(request);
             Object[] result = BeanUtil.deserialize(response, new Class[]{Booking.class}, new DefaultObjectSupplier());
             List<Booking> bookings = new ArrayList<Booking>();
             for (Object element : result) {
                 bookings.add((Booking) element);
             }
             return bookings;
         }
         catch (AxisFault e) {
             e.printStackTrace();
         }
         return null;
     }
 
     @Override
     public Vacation getVacationById(Integer id) {
         QName opFindHotel = new QName(NAMESPACE_URI, "getVacationById");
        Object[] opArgs = new Object[]{id};
         OMElement request = BeanUtil.getOMElement(opFindHotel, opArgs, null, false, null);
         try {
             OMElement response = sender.sendReceive(request);
             Object[] result = BeanUtil.deserialize(response, new Class[]{Vacation.class}, new DefaultObjectSupplier());
             if (result.length == 1) {
                 return (Vacation) result[0];
             }
 
         }
         catch (AxisFault e) {
             e.printStackTrace();
         }
         return null;
     }
 
     @Override
     public void rateVacation(Integer sessionKey, Integer bookingId, Integer rating, String comment) {
         QName opFindHotel = new QName(NAMESPACE_URI, "rateVacation");
         Object[] opArgs = new Object[]{sessionKey, bookingId, rating, comment};
         OMElement request = BeanUtil.getOMElement(opFindHotel, opArgs, null, false, null);
         try {
             sender.sendReceive(request);
         }
         catch (AxisFault e) {
             e.printStackTrace();
         }
     }
 
     @Override
     public void cancelBooking(Integer sessionKey, Integer bookingId) {
         QName opFindHotel = new QName(NAMESPACE_URI, "cancelBooking");
         Object[] opArgs = new Object[]{sessionKey, bookingId};
         OMElement request = BeanUtil.getOMElement(opFindHotel, opArgs, null, false, null);
         try {
             sender.sendReceive(request);
         }
         catch (AxisFault e) {
             e.printStackTrace();
         }
     }
 
     @Override
     public List<Vacation> findVacations(SearchParams params) {
         QName opFindHotel = new QName(NAMESPACE_URI, "findVacations");
         Object[] opArgs = new Object[]{params};
         OMElement request = BeanUtil.getOMElement(opFindHotel, opArgs, null, false, null);
         try {
             OMElement response = sender.sendReceive(request);
             Object[] result = BeanUtil.deserialize(response, new Class[]{Vacation.class}, new DefaultObjectSupplier());
             List<Vacation> vacations = new ArrayList<Vacation>();
             for (Object element : result) {
                 vacations.add((Vacation) element);
             }
             return vacations;
         }
         catch (AxisFault e) {
             e.printStackTrace();
         }
         return null;
     }
 
     @Override
     public Integer createBooking(Integer sessionKey, Integer vacationId, Date startdate, List<Traveler> travelers) {
         QName opFindHotel = new QName(NAMESPACE_URI, "createBooking");
         Object[] opArgs = new Object[]{sessionKey, vacationId, startdate, travelers};
         OMElement request = BeanUtil.getOMElement(opFindHotel, opArgs, null, false, null);
         try {
             OMElement response = sender.sendReceive(request);
             Object[] result = BeanUtil.deserialize(response, new Class[]{Integer.class}, new DefaultObjectSupplier());
             if (result.length == 1) {
                 return (Integer) result[0];
             }
         }
         catch (AxisFault e) {
             e.printStackTrace();
         }
         return null;
     }
 
     @Override
     public Customer getUserInfo(Integer sessionKey) {
         QName opFindHotel = new QName(NAMESPACE_URI, "getUserInfo");
         Object[] opArgs = new Object[]{sessionKey};
         OMElement request = BeanUtil.getOMElement(opFindHotel, opArgs, null, false, null);
         try {
             OMElement response = sender.sendReceive(request);
             Object[] result = BeanUtil.deserialize(response, new Class[]{Customer.class}, new DefaultObjectSupplier());
             if (result.length == 1) {
                 return (Customer) result[0];
             }
         }
         catch (AxisFault e) {
             e.printStackTrace();
         }
         return null;
     }
 }
