 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.eboy.action.user;
 
 import com.ebay.services.finding.Amount;
 import com.eboy.api.ExchangeConversion;
 import com.eboy.po.Item;
 import com.eboy.po.Order;
 import com.eboy.service.ItemService;
 import com.eboy.service.OrderService;
 import com.opensymphony.xwork2.ActionSupport;
import java.math.BigDecimal;
import java.math.RoundingMode;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.Properties;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.mail.Authenticator;
 import javax.mail.BodyPart;
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.PasswordAuthentication;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.AddressException;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeBodyPart;
 import javax.mail.internet.MimeMessage;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.struts2.ServletActionContext;
 
 /**
  *
  * @author wjl
  */
 public class PlaceOrderAction extends ActionSupport {
 
         private OrderService orderService;
         private ItemService itemService;
         private String orderAddress;
         private String orderPhone;
         private String orderReceiver;
         private String orderEmail;
         private String message;
         @Override
         public String execute() throws UnknownHostException {
                 Cookie[] cookie = ServletActionContext.getRequest().getCookies();
                 HttpServletResponse response = ServletActionContext.getResponse();
                 int i, j, cartSize = 0;
                 System.out.println(cookie.length);
                 for (i = 0; i < cookie.length; i++) {
                         System.out.println(cookie[i].getName());
                         if (cookie[i].getName().equals("cartSize")) {
                                 cartSize = Integer.parseInt(cookie[i].getValue());
                                 break;
                         }
                 }
                 if (cartSize == 0) {
                         return "Nothing in the Cart!";
                 }
                 Integer[] itemQuantity = new Integer[cartSize];
                 Integer[] itemId = new Integer[cartSize];
                 Item[] items = new Item[cartSize];
                 for (i = 0; i < cartSize; i++) {
 
                         for (j = 0; j < cookie.length; j++) {
                                 if (cookie[j].getName().equals("item" + i)) {
                                         itemId[i] = Integer.parseInt(cookie[j].getValue());
                                 }
                                 if (cookie[j].getName().equals("item" + i + "Quantity")) {
                                         itemQuantity[i] = Integer.parseInt(cookie[j].getValue());
                                 }
                         }
                         items[i] = getItemService().getItem(itemId[i]);
                         if (items[i].getItemQuantity() < itemQuantity[i]) {
                                 setMessage(items[i].getItemTitle() + " 库存不够了，请重新调整购买数量");
                                 return "Request Too Much";
                         }
                 }
                 Cookie c;
                 for (i = 0; i < cartSize; i++) {
                         Order order = this.placeOrder(itemId[i], itemQuantity[i]);
                         c = new Cookie("item" + i, null);
                         c.setMaxAge(0);
                         c.setPath("/Eboy/");
                         response.addCookie(c);
                         c = new Cookie("item" + i + "Quantity", null);
                         c.setMaxAge(0);
                         c.setPath("/Eboy/");
                         response.addCookie(c);
                         sendMail(order);
                 }
                 c = new Cookie("cartSize", "0");
                 c.setMaxAge(3600 * 24 * 30);
                 c.setPath("/Eboy/");
                 response.addCookie(c);
                 return "success";
         }
 
         public void sendMail(Order order) throws UnknownHostException {
                 Properties props = System.getProperties();
                 props.put("mail.smtp.host", "smtp.gmail.com");
                 props.put("mail.smtp.user", "ebayproject.localize");
                 props.put("mail.smtp.password", "Ebay123456");
                 props.put("mail.smtp.port", "587");
                 props.put("mail.smtp.auth", "true");
                 props.put("mail.smtp.starttls.enable","true");
                 props.put("mail.smtp.Enable", "true");
                 props.put("mail.smtp.socketFactory.fallback", "false");
                 String[] to = {order.getOrderEmail()};
                 Session session = Session.getDefaultInstance(props, null);
                 MimeMessage message = new MimeMessage(session);
                 try {
                         message.setContent(to, NONE);
                         message.setFrom(new InternetAddress("ebayproject.localize"));
                         InternetAddress[] toAddress = new InternetAddress[to.length];
                         for (int i = 0; i < to.length; i++) {
                                 toAddress[i] = new InternetAddress(to[i]);
                         }
                         System.out.println(Message.RecipientType.TO);
 
                         for (int i = 0; i < toAddress.length; i++) {
                                 message.addRecipient(Message.RecipientType.TO, toAddress[i]);
                         }
                         InetAddress addr = InetAddress.getLocalHost();
                         String ip=addr.getHostAddress().toString();//获得本机IP
                         HttpServletRequest request = ServletActionContext.getRequest();
                         int port = request.getLocalPort();
                        BigDecimal b = new BigDecimal(order.getOrderPrice());
                        double orderPrice = b.setScale(2, RoundingMode.HALF_UP).doubleValue();
                        
                        
                        
                         message.setSubject("成功添加Eboy订单");
                         String content = "<h1>Eboy订单通知</h1>"
                                 + "<p>感谢您在Eboy上购物</p>"
                                 + "<p>您刚才购买的商品已经生成订单，即将准备发货</p>"
                                 + "<p>您可以通过如下订单号以及订单验证码在我们网站上在线查询</p>"
                                 + "<p>当发货后，您还可以及时了解商品的物流信息</p>"
                                 + "<p>您购买的商品是"
                                 + order.getItem().getItemTitle()
                                 +"</p>"
                                 + "<p>价格为 "
                                + orderPrice
                                + " RMB</p>"
                                 + "<p>您的订单号为 " + order.getOrderId() + "</p>" 
                                 + "<p>您的验证码为 " + order.getOrderValidate() + "</p>"
                                 + "<p>当然您也可以通过如下二维码在手机上直接查阅订单信息</p>"
                                 + "<img src=https://chart.googleapis.com/chart?cht=qr&chs=200x200&choe=UTF-8&chld=L|4&chl="
                                 + ip
                                 + ":" + port +"/Eboy/jsp/user/mobileGetOrder.action?orderSecret="
                                 + order.getOrderValidate() + order.getOrderId()
                                 + "&/>";
                         message.setContent(content,"text/html;charset=utf-8");
                         Transport transport = session.getTransport("smtp");
                         transport.connect("smtp.gmail.com", "ebayproject.localize", "Ebay123456");
                         transport.sendMessage(message, message.getAllRecipients());
                         transport.close();
                 } catch (MessagingException ex) {
                         Logger.getLogger(PlaceOrderAction.class.getName()).log(Level.SEVERE, null, ex);
                 }
 
 
 
 
 //                
 //                try {
 //                        InternetAddress from = new InternetAddress("ebayproject.localize@gmail.com");
 //                        message.setFrom(from);
 //
 //                        InternetAddress to = new InternetAddress(getOrderEmail());
 //                        message.setRecipient(Message.RecipientType.TO, to);
 //
 //                        message.setSubject("成功添加订单");
 //                        String content = "您的订单号为 " + order.getOrderId() + ", 认证码为 " + order.getOrderValidate();
 //                        message.setContent(content, "text/plan;charset=utf8");
 //                        message.saveChanges();
 //                        Transport transport = s.getTransport("smtp");
 //                        transport.connect("smtp.gmail.com", "ebayproject.localize", "Ebay123456");
 //                        transport.sendMessage(message, message.getAllRecipients());
 //                        transport.close();
 //                } catch (AddressException ex) {
 //                        Logger.getLogger(PlaceOrderAction.class.getName()).log(Level.SEVERE, null, ex);
 //                } catch (MessagingException ex) {
 //                        Logger.getLogger(PlaceOrderAction.class.getName()).log(Level.SEVERE, null, ex);
 //                }
         }
 
         public Order placeOrder(int itemId, int itemQuantity) {
                 int quantity = itemQuantity;
                 Item item = getItemService().getItem(itemId);
 
                 Order order = new Order();
                 order.setItem(item);
                 order.setOrderQuantity(quantity);
 
                 char[] validateCode = new char[62];
                 for (int i = 0; i < 10; i++) {
                         validateCode[i] = (char) (48 + i);
                 }
                 for (int i = 0; i < 26; i++) {
                         validateCode[10 + i] = (char) (65 + i);
                 }
                 for (int i = 0; i < 26; i++) {
                         validateCode[36 + i] = (char) (97 + i);
                 }
                 String validate = "";
                 for (int i = 0; i < 8; i++) {
                         int index = (int) (Math.random() * 62);
                         validate = validate + validateCode[index];
                 }
                 order.setOrderValidate(validate);
                 Amount amount, newAmount;
                 double money = 0;
                 amount = new Amount();
                 amount.setValue(item.getItemPrice());
                 amount.setCurrencyId(item.getItemPriceCurrency());
                 newAmount = ExchangeConversion.execute(amount);
                 money += newAmount.getValue();
                 if (item.getItemPackageCost() != null) {
                         amount = new Amount();
                         amount.setValue(item.getItemPackageCost());
                         amount.setCurrencyId(item.getItemPackageCostCurrency());
                         newAmount = ExchangeConversion.execute(amount);
                         money += newAmount.getValue();
                 }
                 if (item.getItemImportCost() != null) {
                         amount = new Amount();
                         amount.setValue(item.getItemImportCost());
                         amount.setCurrencyId(item.getItemImportCostCurrency());
                         newAmount = ExchangeConversion.execute(amount);
                         money += newAmount.getValue();
                 }
                 if (item.getItemShippingCost() != null) {
                         amount = new Amount();
                         amount.setValue(item.getItemShippingCost());
                         amount.setCurrencyId(item.getItemShippingCostCurrency());
                         newAmount = ExchangeConversion.execute(amount);
                         money += newAmount.getValue();
                 }
                 if (item.getItemInsuranceCost() != null) {
                         amount = new Amount();
                         amount.setValue(item.getItemInsuranceCost());
                         amount.setCurrencyId(item.getItemInsuranceCostCurrency());
                         newAmount = ExchangeConversion.execute(amount);
                         money += newAmount.getValue();
                 }
                 if (item.getItemTaxCost() != null) {
                         amount = new Amount();
                         amount.setValue(item.getItemTaxCost());
                         amount.setCurrencyId(item.getItemTaxCostCurrency());
                         newAmount = ExchangeConversion.execute(amount);
                         money += newAmount.getValue();
                 }
                 order.setOrderPrice(money * quantity);
                 order.setOrderAddress(getOrderAddress());
                 order.setOrderStatus("未发货");
                 order.setOrderPhone(getOrderPhone());
                 order.setOrderReceiver(getOrderReceiver());
                 order.setOrderEmail(getOrderEmail());
                 getOrderService().saveOrder(order);
                 item.setItemQuantity(item.getItemQuantity() - quantity);
                 item.setItemSoldQuantity(item.getItemSoldQuantity() + quantity);
                 getItemService().updateItem(item);
                 return order;
         }
 
         public OrderService getOrderService() {
                 return orderService;
         }
 
         public void setOrderService(OrderService orderService) {
                 this.orderService = orderService;
         }
 
         public ItemService getItemService() {
                 return itemService;
         }
 
         public void setItemService(ItemService itemService) {
                 this.itemService = itemService;
         }
 
         public String getOrderAddress() {
                 return orderAddress;
         }
 
         public void setOrderAddress(String orderAddress) {
                 this.orderAddress = orderAddress;
         }
 
         public String getOrderPhone() {
                 return orderPhone;
         }
 
         public void setOrderPhone(String orderPhone) {
                 this.orderPhone = orderPhone;
         }
 
         public String getOrderReceiver() {
                 return orderReceiver;
         }
 
         public void setOrderReceiver(String orderReceiver) {
                 this.orderReceiver = orderReceiver;
         }
 
         public String getOrderEmail() {
                 return orderEmail;
         }
 
         public void setOrderEmail(String orderEmail) {
                 this.orderEmail = orderEmail;
         }
 
         public String getMessage() {
                 return message;
         }
 
         public void setMessage(String message) {
                 this.message = message;
         }
 }
