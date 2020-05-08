 /*
  * Copyright (c) 2005 Your Corporation. All Rights Reserved.
  */
 package dwr;
 
 import org.wings.session.Session;
 import org.wings.session.SessionManager;
 import org.wings.externalizer.ExternalizedResource;
 import org.wings.event.SRequestEvent;
 import org.wings.event.SRequestListener;
 import org.wings.*;
 import org.wings.plaf.css.DWRScriptListener;
 import org.wings.resource.DefaultURLResource;
 import org.wings.header.Script;
 import org.wings.script.JavaScriptListener;
 import org.wings.script.JavaScriptEvent;
 import org.wings.text.SAbstractFormatter;
 
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import java.text.ParseException;
 import java.text.DateFormat;
 import java.text.NumberFormat;
 import java.io.IOException;
 
 import dwr.domain.AutoCompletionDummy;
 
 /**
  * @author hengels
  * @version $Revision$
  */
 public class DWRExample
 {
     private SFormattedTextField dateTextField;
     private SFormattedTextField numberTextField;
     private STextField countryInput;
     private SComboBox countryBox;
     private SButton button;
     private SFrame frame;
 
     public DWRExample() throws IOException {
         dateTextField = new SFormattedTextField(new DateFormatter());
         dateTextField.setName("dateTextField");
 
         numberTextField = new SFormattedTextField(new NumberFormatter());
         numberTextField.setName("numberTextField");
 
         countryInput = new STextField();
         countryInput.setName("countryInput");
 
         countryBox = new SComboBox();
         countryBox.setName("countryBox");
 
         String name = "countrySelection_" + System.identityHashCode(countryInput);
         countryInput.addScriptListener(new DWRScriptListener(
                 JavaScriptEvent.ON_KEY_UP,
                name + ".getData(DWRUtil.getValue('" + countryInput.getName() + "_input'),          \n" +
                        "                                                                           \n" +
                        "function(countries) {                                                      \n" +
                        "    DWRUtil.removeAllOptions('" + countryBox.getName() + "_select');       \n" +
                        "    DWRUtil.addOptions('" + countryBox.getName() + "_select', countries);  \n" +
                        "});                                                                        \n",
                 name,
                 new AutoCompletionDummy()
         ));
 
         button = new SButton("submit");
 
         SForm form = new SForm();
         form.setLayout(new STemplateLayout(SessionManager.getSession().getServletContext().getRealPath("/template/content.thtml")));
         form.add(dateTextField, "dateTextField");
         form.add(numberTextField, "numberTextField");
         form.add(countryInput, "countryInput");
         form.add(countryBox, "countryBox");
         form.add(button, "submit");
 
         frame = new SFrame("DWRExample example");
         frame.getContentPane().add(form);
         frame.show();
         frame.addHeader(new Script("text/javascript", new DefaultURLResource("../dwr/engine.js")));
         frame.addHeader(new Script("text/javascript", new DefaultURLResource("../dwr/util.js")));
     }
 
     public static class DateFormatter extends SAbstractFormatter {
         DateFormat format = DateFormat.getDateInstance(DateFormat.SHORT, SessionManager.getSession().getLocale());
 
         public Object stringToValue(String text) throws ParseException {
             if (text == null || text.trim().length() == 0)
                 return null;
             else
                 return format.parse(text.trim());
         }
 
         public String valueToString(Object value) throws ParseException {
             if (value == null)
                 return "";
             else
                 return format.format(value);
         }
     }
 
     public static class NumberFormatter extends SAbstractFormatter {
         NumberFormat format = NumberFormat.getNumberInstance(SessionManager.getSession().getLocale());
 
         public Object stringToValue(String text) throws ParseException {
             if (text == null || text.trim().length() == 0)
                 return null;
             else
                 return format.parse(text.trim());
         }
 
         public String valueToString(Object value) throws ParseException {
             if (value == null)
                 return "";
             else
                 return format.format(value);
         }
     }
 }
