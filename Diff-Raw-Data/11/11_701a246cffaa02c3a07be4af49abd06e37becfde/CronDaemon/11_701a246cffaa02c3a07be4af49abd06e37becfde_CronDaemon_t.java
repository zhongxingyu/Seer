 /*
  * Copyright (c) 2002 John Vasileff
  *
  * Permission  is  hereby  granted,  free of  charge,  to  any  person
  * obtaining  a copy  of  this software  and associated  documentation
  * files   (the  "Software"),   to  deal   in  the   Software  without
  * restriction, including without limitation  the rights to use, copy,
  * modify, merge, publish, distribute,  sublicense, and/or sell copies
  * of the  Software, and  to permit  persons to  whom the  Software is
  * furnished to do so, subject to the following conditions:
  *
  * The  above copyright  notice and  this permission  notice shall  be
  * included in all copies or substantial portions of the Software.
  *
  * THE  SOFTWARE  IS  PROVIDED  "AS   IS",  WITHOUT  WARRANTY  OF  ANY
  * KIND,  EXPRESS  OR  IMPLIED,  INCLUDING  BUT  NOT  LIMITED  TO  THE
  * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
  * BE LIABLE FOR ANY CLAIM, DAMAGES  OR OTHER LIABILITY, WHETHER IN AN
  * ACTION OF CONTRACT,  TORT OR OTHERWISE, ARISING FROM, OUT  OF OR IN
  * CONNECTION WITH  THE SOFTWARE OR THE  USE OR OTHER DEALINGS  IN THE
  * SOFTWARE.
  */
 
 package org.anodyneos.jse.cron;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.TimeZone;
 import java.util.UUID;
 
 import javax.xml.XMLConstants;
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBElement;
 import javax.xml.bind.UnmarshalException;
 import javax.xml.bind.Unmarshaller;
 import javax.xml.bind.ValidationEvent;
 import javax.xml.bind.ValidationEventLocator;
 import javax.xml.bind.util.ValidationEventCollector;
 import javax.xml.transform.Source;
 import javax.xml.transform.stream.StreamSource;
 import javax.xml.validation.Schema;
 import javax.xml.validation.SchemaFactory;
 
 import org.anodyneos.jse.JseDateAwareJob;
 import org.anodyneos.jse.JseException;
 import org.anodyneos.jse.JseTimerService;
 import org.anodyneos.jse.cron.config.Config;
 import org.anodyneos.jse.cron.config.Job;
 import org.anodyneos.jse.cron.config.Property;
 import org.anodyneos.jse.cron.config.Schedule;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.MutablePropertyValues;
 import org.springframework.beans.factory.support.GenericBeanDefinition;
 import org.xml.sax.InputSource;
 
 public class CronDaemon {
 
     private static final Log log = LogFactory.getLog(CronDaemon.class);
 
     private ArrayList<JseTimerService> timerServices = new ArrayList<JseTimerService>();
 
     public CronDaemon(InputSource source) throws JseException {
 
         Schedule schedule;
 
         // parse source
         try {
 
             JAXBContext jc = JAXBContext.newInstance( "org.anodyneos.jse.cron.config" );
             Unmarshaller u = jc.createUnmarshaller();
             //Schedule
             Source schemaSource = new StreamSource(
                     Thread.currentThread().getContextClassLoader().getResourceAsStream(
                             "org/anodyneos/jse/cron/cron.xsd"));
 
             SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
             Schema schema = sf.newSchema(schemaSource);
             u.setSchema(schema);
             ValidationEventCollector vec = new ValidationEventCollector();
             u.setEventHandler(vec);
 
             JAXBElement<?> rootElement;
             try {
                 rootElement = ((JAXBElement<?>)u.unmarshal(source));
             } catch (UnmarshalException ex) {
                 if (! vec.hasEvents()) {
                     throw ex;
                 } else {
                     for (ValidationEvent ve : vec.getEvents()) {
                         ValidationEventLocator vel = ve.getLocator();
                         log.error("Line:Col[" + vel.getLineNumber() +
                             ":" + vel.getColumnNumber() +
                             "]:" + ve.getMessage());
                     }
                     throw new JseException(
                             "Validation failed for source publicId='" + source.getPublicId()
                             + "'; systemId='" + source.getSystemId() + "';");
                 }
             }
 
             schedule = (Schedule) rootElement.getValue();
 
             if (vec.hasEvents()) {
                 for (ValidationEvent ve : vec.getEvents()) {
                     ValidationEventLocator vel = ve.getLocator();
                     log.warn("Line:Col[" + vel.getLineNumber() +
                         ":" + vel.getColumnNumber() +
                         "]:" + ve.getMessage());
                 }
             }
 
         } catch (JseException e) {
             throw e;
         } catch (Exception e) {
             throw new JseException("Cannot parse " + source + ".", e);
         }
 
         SpringHelper springHelper = new SpringHelper();
 
         ////////////////
         //
         // Configure Spring and Create Beans
         //
         ////////////////
 
         TimeZone defaultTimeZone;
 
         if(schedule.isSetTimeZone()) {
             defaultTimeZone = getTimeZone(schedule.getTimeZone());
         } else {
             defaultTimeZone = TimeZone.getDefault();
         }
 
         if(schedule.isSetSpringContext() && schedule.getSpringContext().isSetConfig()) {
             for (Config config : schedule.getSpringContext().getConfig()) {
                 springHelper.addXmlClassPathConfigLocation(config.getClassPathResource());
             }
         }
 
         for(org.anodyneos.jse.cron.config.JobGroup jobGroup : schedule.getJobGroup()) {
             for (Job job : jobGroup.getJob()) {
                 if(job.isSetBeanRef()) {
                     if (job.isSetBean() || job.isSetClassName()) {
                         throw new JseException("Cannot set bean or class attribute for job when beanRef is set.");
                     } // else config ok
                 } else {
                     if (!job.isSetClassName()) {
                         throw new JseException("must set either class or beanRef for job.");
                     }
                     GenericBeanDefinition beanDef = new GenericBeanDefinition();
                     MutablePropertyValues propertyValues = new MutablePropertyValues();
 
                     if (!job.isSetBean()) {
                         job.setBean(UUID.randomUUID().toString());
                     }
 
                     if (springHelper.containsBean(job.getBean())) {
                         throw new JseException("Bean name already used; overriding not allowed here: " + job.getBean());
                     }
 
                     beanDef.setBeanClassName(job.getClassName());
 
                     for(Property prop : job.getProperty()) {
                         String value = null;
                         if (prop.isSetSystemProperty()) {
                             value = System.getProperty(prop.getSystemProperty());
                         }
                         if (null == value) {
                             value = prop.getValue();
                         }
 
                         propertyValues.addPropertyValue(prop.getName(), value);
                     }
 
                     beanDef.setPropertyValues(propertyValues);
                     springHelper.registerBean(job.getBean(), beanDef);
                     job.setBeanRef(job.getBean());
                 }
             }
         }
 
         springHelper.init();
 
         ////////////////
         //
         // Configure Timer Services
         //
         ////////////////
 
         for(org.anodyneos.jse.cron.config.JobGroup jobGroup : schedule.getJobGroup()) {
 
             String jobGroupName;
             JseTimerService service = new JseTimerService();
 
             timerServices.add(service);
 
 
             if (jobGroup.isSetName()) {
                 jobGroupName = jobGroup.getName();
             } else {
                 jobGroupName = UUID.randomUUID().toString();
             }
 
             if(jobGroup.isSetMaxConcurrent()) {
                 service.setMaxConcurrent(jobGroup.getMaxConcurrent());
             }
 
             for (Job job : jobGroup.getJob()) {
 
                 TimeZone jobTimeZone = defaultTimeZone;
 
                 if (job.isSetTimeZone()) {
                     jobTimeZone = getTimeZone(job.getTimeZone());
                 } else {
                     jobTimeZone = defaultTimeZone;
                 }
 
                 Object obj;
 
                 Date notBefore = null;
                 Date notAfter = null;
 
                 if(job.isSetNotBefore()) {
                     notBefore = job.getNotBefore().toGregorianCalendar(jobTimeZone, null, null).getTime();
                 }
                 if(job.isSetNotAfter()) {
                     notAfter = job.getNotAfter().toGregorianCalendar(jobTimeZone, null, null).getTime();
                 }
 
                 CronSchedule cs = new CronSchedule(
                         job.getSchedule(),
                         jobTimeZone,
                         job.getMaxIterations(),
                         job.getMaxQueue(),
                         notBefore,
                         notAfter);
 
 
                 obj = springHelper.getBean(job.getBeanRef());
                 log.info("Adding job " + jobGroup.getName() + "/" + job.getName() + " using bean " + job.getBeanRef());
                 if (obj instanceof CronJob) {
                     ((CronJob) obj).setCronContext(new CronContext(jobGroupName, job.getName(), cs));
                 }
                 if (obj instanceof JseDateAwareJob) {
                     service.createTimer((JseDateAwareJob) obj, cs);
                 } else if (obj instanceof Runnable) {
                     service.createTimer((Runnable) obj, cs);
                 } else {
                     throw new JseException("Job must implement Runnable or JseDateAwareJob");
                 }
             }
         }
     }
 
     public void start() {
         Iterator<JseTimerService> it = timerServices.iterator();
         while(it.hasNext()) {
             JseTimerService service = it.next();
             log.info("Starting service thread: " + service.getName());
             service.start();
         }
     }
 
     public static void main(String[] args) throws Exception {
        try {
            InputSource source = new InputSource(args[0]);
            CronDaemon server = new CronDaemon(source);
            server.start();
        } catch (Exception e) {
            log.fatal(e.getMessage(), e);
            throw e;
        }
     }
 
     protected TimeZone getTimeZone(String tzs) throws JseException {
         String[] allTZ = TimeZone.getAvailableIDs();
         TimeZone tz = null;
         for(int i = 0; i < allTZ.length; i++) {
             if(allTZ[i].equals(tzs)) {
                 tz = TimeZone.getTimeZone(tzs);
                 break;
             }
         }
         if (null == tz) {
             throw new JseException("TimeZone not available: " + tzs);
         }
         return tz;
     }
 
 
 }
