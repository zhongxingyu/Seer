 package net.transformatorhuis.teapot;
 
 import java.net.URL;
 import java.util.List;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Marshaller;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import net.transformatorhuis.xsd.Color;
 import net.transformatorhuis.xsd.Display;
 import net.transformatorhuis.xsd.Lightsource;
 import net.transformatorhuis.xsd.ObjectFactory;
 import net.transformatorhuis.xsd.Param;
 import net.transformatorhuis.xsd.Projection;
 import net.transformatorhuis.xsd.Rib;
 import net.transformatorhuis.xsd.Rotate;
 import net.transformatorhuis.xsd.Surface;
 import net.transformatorhuis.xsd.Translate;
 import net.transformatorhuis.xsd.World;
 
 import org.apache.log4j.Logger;
 import org.apache.log4j.xml.DOMConfigurator;
 import org.w3c.dom.Document;
 
 
 /**
  * 
  * This class will create Teapot.xml 'staticly' via jaxb.
  * 
  * @author cyberroadie
  * 
  */
 public class Teapot {
 
     /**
      * Logger.
      */
     private Logger logger;
 
     /**
      * Rib.
      */
     private Rib rib;
 
     /**
      * Marshaller.
      */
     private Marshaller m;
 
     /**
      * @throws BrokenTeapotException broken teapot
      */
     public Teapot() throws BrokenTeapotException {
         logger = Logger.getLogger(Teapot.class);
         /* First try to get it out of jar file */
         Class clazz = getClass();
         URL url = clazz.getResource("/conf/log4j.xml");
         if (url == null) {
             /* Try reading from working directory */
             DOMConfigurator.configure("conf/log4j.xml");
         } else {
             DOMConfigurator.configure(url);
         }
 
         try {
            JAXBContext jc = JAXBContext.newInstance("net.transformatorhuis.xsd");
             m = jc.createMarshaller();
             m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
             // creating the ObjectFactory
            ObjectFactory objFactory = new ObjectFactory();
 
             // Intial setup renderman file
             rib = objFactory.createRib();
             rib.setVersion("3.03");
 
             List ribList = rib.getDisplayOrProjectionOrRotate();
 
             Display display = objFactory.createDisplay();
             display.setName("teapot.tiff");
             display.setType("file");
             display.setMode("rgb");
             ribList.add(display);
 
             Projection projection = objFactory.createProjection();
             projection.setName("perspective");
             ribList.add(projection);
 
             Translate translate = objFactory.createTranslate();
             translate.setDx(0);
             translate.setDy(0);
             translate.setDz(25);
             ribList.add(translate);
 
             Rotate rotate = objFactory.createRotate();
             rotate.setAngle(-22);
             rotate.setDx(1);
             rotate.setDy(0);
             rotate.setDz(0);
             ribList.add(rotate);
 
             Rotate rotate2 = objFactory.createRotate();
             rotate2.setAngle(19);
             rotate2.setDx(0);
             rotate2.setDy(1);
             rotate2.setDz(0);
             ribList.add(rotate2);
 
             Translate translate2 = objFactory.createTranslate();
             translate2.setDx(0);
             translate2.setDy(-3);
             translate2.setDz(25);
             ribList.add(translate2);
 
             // World
             World world = objFactory.createWorld();
             List worldList = world.getLightsourceOrSurfaceOrColor();
 
             Lightsource ambientLightSource = objFactory.createLightsource();
             ambientLightSource.setShadername("ambientlight");
             List ambientLightSourceParamList = ambientLightSource.getParam();
             Param param = objFactory.createParam();
             param.setName("intensity");
             param.setValue("0.4");
             ambientLightSourceParamList.add(param);
             worldList.add(ambientLightSource);
 
             Lightsource distantLightSource = objFactory.createLightsource();
             distantLightSource.setShadername("distantlight");
             List distantLightSourceParamList = distantLightSource.getParam();
             // TODO new sollution for naming param2
             Param param2 = objFactory.createParam(); 
             param2.setName("intensity");
             // TODO can this be a float? This is not type safe
             param2.setValue("0.6"); 
             
             distantLightSourceParamList.add(param2);
             worldList.add(distantLightSource);
 
             Surface surface = objFactory.createSurface();
             surface.setShadername("plastic");
             worldList.add(surface);
 
             Color color = objFactory.createColor();
             // TODO can this be a float? This is not type safe
             color.setR("1");
             color.setG("0.6");
             color.setB("0");
             worldList.add(color);
 
             Spout spout = new Spout(objFactory);
             worldList.add(spout.getJAXB());
             Handle handle = new Handle(objFactory);
             worldList.add(handle.getJAXB());
 
             Body body = new Body(objFactory);
             worldList.add(body.getJAXB());
 
             Top top = new Top(objFactory);
             worldList.add(top.getJAXB());
 
             ribList.add(world);
 
         } catch (JAXBException e) {
             throw new BrokenTeapotException(e.toString());
         }
 
     }
 
     /**
      * @return DOM document
      * @throws BrokenTeapotException broken teapot
      */
     public Document toDOM() throws BrokenTeapotException {
 
         DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
         dbf.setNamespaceAware(true);
        DocumentBuilder db;
        Document doc;
         try {
             db = dbf.newDocumentBuilder();
             doc = db.newDocument();
             m.marshal(rib, doc);
         } catch (ParserConfigurationException e) {
             throw new BrokenTeapotException(e.toString());
         } catch (JAXBException e) {
             throw new BrokenTeapotException(e.toString());
         }
 
         return doc;
     }
 
     /**
      * Writes to system.
      * @throws BrokenTeapotException broken teapot
      */
     public void toSystemOut() throws BrokenTeapotException {
 
         try {
             m.marshal(rib, System.out);
         } catch (JAXBException e) {
             throw new BrokenTeapotException(e.toString());
         }
 
     }
 
 }
