 package net.alpha01.jwtest.exports;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Iterator;
 
 import ooo.connector.BootstrapSocketConnector;
 
 import com.sun.star.beans.PropertyValue;
 import com.sun.star.comp.helper.BootstrapException;
 import com.sun.star.container.XIndexAccess;
 import com.sun.star.container.XNameAccess;
 import com.sun.star.frame.XComponentLoader;
 import com.sun.star.frame.XController;
 import com.sun.star.frame.XModel;
 import com.sun.star.frame.XStorable;
 import com.sun.star.lang.IndexOutOfBoundsException;
 import com.sun.star.lang.XComponent;
 import com.sun.star.lang.XMultiComponentFactory;
 import com.sun.star.sdbc.SQLException;
 import com.sun.star.sdbc.XCloseable;
 import com.sun.star.sheet.XSpreadsheet;
 import com.sun.star.sheet.XSpreadsheetDocument;
 import com.sun.star.sheet.XSpreadsheetView;
 import com.sun.star.uno.Exception;
 import com.sun.star.uno.UnoRuntime;
 import com.sun.star.uno.XComponentContext;
 
 import net.alpha01.jwtest.JWTestSession;
 import net.alpha01.jwtest.beans.Project;
 import net.alpha01.jwtest.beans.Requirement;
 import net.alpha01.jwtest.dao.RequirementMapper;
 import net.alpha01.jwtest.dao.SqlConnection;
 import net.alpha01.jwtest.dao.SqlSessionMapper;
 import net.alpha01.jwtest.dao.RequirementMapper.RequirementSelectSort;
 import net.alpha01.jwtest.exceptions.JWTestException;
 import net.alpha01.jwtest.util.JWTestConfig;
 import au.com.bytecode.opencsv.CSVWriter;
 
 public class RequirementODSExporter {
 	/**
 	 * 
 	 * @throws JWTestException
 	 * @throws IndexOutOfBoundsException
 	 * @throws BootstrapException
 	 */
 	public static File exportToODS() throws JWTestException{
 		File tmpFile;
 		try {
 			
 			tmpFile = File.createTempFile("jwtest_export_requirement", ".ods");
 			
 			tmpFile.deleteOnExit();
 			XComponent xComponent=exportToXComponent();
 			storeDocComponent(xComponent, "file:///"+tmpFile.getAbsolutePath().replace('\\', '/'));
 			closeDocComponent(xComponent);
 			return tmpFile;
 		
 		} catch (IOException e) {
 			throw new JWTestException(RequirementODSExporter.class,e);
 		}  catch (BootstrapException e) {
 			throw new JWTestException(RequirementODSExporter.class,e);
 		}
 		catch (java.lang.Exception e) {
 			throw new JWTestException(RequirementODSExporter.class,e);
 		}
 	}
 	/**
 	 * 
 	 * @return
 	 * @throws JWTestException
 	 * @throws Exception 
 	 * @throws BootstrapException 
 	 */
 	public static XComponent exportToXComponent() throws JWTestException, BootstrapException, Exception{
 		Project prj = ((JWTestSession)JWTestSession.get()).getCurrentProject();
 		SqlSessionMapper<RequirementMapper> sesMapper = SqlConnection.getSessionMapper(RequirementMapper.class);
 		Iterator<Requirement> itr = sesMapper.getMapper().getAll(new RequirementSelectSort(prj.getId(), "num", true)).iterator();
 		sesMapper.close();
 		XComponent xComponent=createXComponent();
 		XSpreadsheet xSpreadsheet=getSpreadSheet(xComponent, "Requisiti");
 		
 		int y=10;
 	
 		while (itr.hasNext()){
 			Requirement req = itr.next();
 			xSpreadsheet.getCellByPosition(1, y).setFormula(req.getType().toString());
 			xSpreadsheet.getCellByPosition(2, y).setFormula(req.getNum().toString());
 			xSpreadsheet.getCellByPosition(5, y).setFormula(req.getName());
 			xSpreadsheet.getCellByPosition(6, y++).setFormula(req.getDescription());
 		}
 		return xComponent;
 	}
 	
 	/**
 	 * 
 	 * @return
 	 * @throws BootstrapException
 	 * @throws Exception
 	 */
 	public static XComponent createXComponent() throws BootstrapException, Exception{
 		String oooExeFolder=JWTestConfig.getProp("openoffice.oooExeFolder");
 		String template=JWTestConfig.getProp("openoffice.template").replace('\\','/');
 		XComponentContext context = BootstrapSocketConnector.bootstrap(oooExeFolder);
 		System.out.println("Trying to connect to OOo service... ");
 		 
 		 
 		// recupera il service manager
 		XMultiComponentFactory factory = context.getServiceManager();
 		String available = (factory != null ? "available" : "not available");
 		
 		System.out.println("the the service is '" +available +"'! ");
 		
 		XComponentLoader xcomponentloader = (XComponentLoader) 
 				UnoRuntime.queryInterface(XComponentLoader.class,factory.createInstanceWithContext("com.sun.star.frame.Desktop", context));
 		
 		 
         PropertyValue[] loadProps = new PropertyValue[1];
         loadProps [0] = new PropertyValue();
         loadProps[0].Name = "Hidden";
         loadProps[0].Value = new Boolean(true);
         
		XComponent xComponent = xcomponentloader.loadComponentFromURL("file://"+template, "_blank", 0,loadProps);
 		return xComponent;
 	}
 	/**
 	 * 
 	 * @return
 	 * @throws BootstrapException
 	 * @throws Exception
 	 */
 	public static XSpreadsheet getSpreadSheet(XComponent xComponent) throws BootstrapException, Exception{
 	
 			XSpreadsheetDocument xSpreadsheetDocument=UnoRuntime.queryInterface(XSpreadsheetDocument.class,xComponent);
 			
 			XModel model = (XModel) UnoRuntime.queryInterface(XModel.class, xSpreadsheetDocument);
 			XController controller = model.getCurrentController();
 		    XSpreadsheetView view = (XSpreadsheetView) UnoRuntime.queryInterface(XSpreadsheetView.class, controller);
 		    XSpreadsheet xSpreadsheet=view.getActiveSheet();
 		    return xSpreadsheet;
 		   
 	}
 	
 	public static XSpreadsheet getSpreadSheet(XComponent xComponent, String name) throws BootstrapException, Exception{
 		
 		XSpreadsheetDocument xSpreadsheetDocument=UnoRuntime.queryInterface(XSpreadsheetDocument.class,xComponent);
 		
 	  Object sheetObj = xSpreadsheetDocument.getSheets().getByName(name); 
       return (XSpreadsheet) UnoRuntime.queryInterface(XSpreadsheet.class, sheetObj);
 	
 	   
 }
 	
 	
 	/**
 	 * 
 	 * @param xDoc
 	 * @param storeUrl
 	 * @throws java.lang.Exception
 	 */
 	 public static  void storeDocComponent(XComponent xDoc, String storeUrl) throws java.lang.Exception {
 	 
 	     XStorable xStorable = (XStorable)UnoRuntime.queryInterface(XStorable.class, xDoc);
 	     PropertyValue[] storeProps = new PropertyValue[1];
 	     storeProps[0] = new PropertyValue();
 	    
 	     xStorable.storeAsURL(storeUrl, storeProps); 
 	 }
 	 
 	 /**
 	  * 
 	  * @param xDoc
 	  * @throws SQLException
 	  */
 	 public static  void closeDocComponent(XComponent xDoc) throws SQLException{
 		 XCloseable xCloseable = (XCloseable)UnoRuntime.queryInterface(XCloseable.class, xDoc);
 		 if (xCloseable!=null){
 			 xCloseable.close();
 		 }else{
 			 xDoc.dispose();
 		 }
 	 }
 }
