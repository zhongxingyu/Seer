 /*
  * Created on Aug 13, 2007
  * @author
  *
  */
 
 package edu.common.dynamicextensions.xmi;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.jmi.reflect.RefPackage;
 import javax.jmi.xmi.XmiWriter;
 import javax.xml.transform.Result;
 import javax.xml.transform.Source;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.collections.Predicate;
 import org.apache.commons.lang.StringUtils;
 import org.netbeans.api.mdr.MDRManager;
 import org.netbeans.api.mdr.MDRepository;
 import org.omg.uml.foundation.core.Attribute;
 import org.omg.uml.foundation.core.Generalization;
 import org.omg.uml.foundation.core.ModelElement;
 import org.omg.uml.foundation.core.UmlAssociation;
 import org.omg.uml.foundation.core.UmlClass;
 import org.omg.uml.modelmanagement.UmlPackage;
 import org.openide.util.Lookup;
 
 import edu.common.dynamicextensions.domaininterface.AssociationInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeInterface;
 import edu.common.dynamicextensions.domaininterface.EntityGroupInterface;
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 
 /**
  * @author preeti_lodha
  * @author ashish_gupta
  *
  * Utility functions for XMI import/XMI 
  */
 public class XMIUtilities
 {/*
  //Common Utility functions
  */
 
 	/**
 	 * @return MDRepository object
 	 */
 	public static MDRepository getRepository()
 	{
 		return MDRManager.getDefault().getDefaultRepository();
 	}
 
 	/**
 	 * Get UML Package
 	 * @param repository
 	 * @param extent
 	 * @return
 	 */
 	public static RefPackage getUMLPackage(MDRepository repository, String extent)
 	{
 		return null;
 	}
 
 	/**
 	 * Get MOF Package 
 	 * @param repository
 	 * @param extent
 	 * @return
 	 */
 	public static RefPackage getMOFPackage(MDRepository repository, String extent)
 	{
 		return null;
 	}
 
 	//XMI Export Related
 	/**
 	 * 
 	 * @param entityGroup
 	 * @return
 	 */
 	public static UmlPackage getUMLPackage(EntityGroupInterface entityGroup)
 	{
 		return null;
 	}
 
 	/**
 	 * Return a UML Class object for given Entity Domain Object
 	 * @param EntityInterface : entity
 	 * @return	 UML Class
 	 */
 	public static UmlClass getUMLClass(EntityInterface entity)
 	{
 		return null;
 	}
 
 	/**
 	 * 
 	 * @param attribute
 	 * @return
 	 */
 	public static Attribute getUMLAttribute(AttributeInterface attribute)
 	{
 		return null;
 	}
 
 	/**
 	 * Return a UML Class object for given Entity Domain Object
 	 * @param association
 	 * @return
 	 */
 	public static UmlAssociation getUMLAssociation(AssociationInterface association)
 	{
 		return null;
 	}
 
 	public static XmiWriter getXMIWriter()
 	{
		return (XmiWriter) Lookup.getDefault().lookup(XmiWriter.class);
 
 	}
 
 	public static String getClassNameForEntity(EntityInterface entity)
 	{
 		if (entity != null)
 		{
 			return entity.getName();
 		}
 		return null;
 	}
 
 	/**
 	 * @return
 	 */
 	public static String getAttributeName(AttributeInterface attribute)
 	{
 		if (attribute != null)
 		{
 			return attribute.getName();
 		}
 		return null;
 	}
 
 	/***
 	 * Finds and returns the first model element having the given
 	 * <code>name</code> in the <code>umlPackage</code>, returns
 	 * <code>null</code> if not found.
 	 *
 	 * @param umlPackage The modelPackage to search
 	 * @param name the name to find.
 	 * @return the found model element.
 	 */
 	public static Object find(org.omg.uml.modelmanagement.UmlPackage umlPackage, final String name)
 	{
 		return CollectionUtils.find(umlPackage.getOwnedElement(), new Predicate()
 		{
 
 			public boolean evaluate(Object object)
 			{
 				return StringUtils.trimToEmpty(((ModelElement) object).getName()).equals(name);
 				//return true;
 			}
 		});
 	}
 
 	/**
 	 * Finds and returns the first model element having the given
 	 * <code>name</code> in the <code>modelPackage</code>, returns
 	 * <code>null</code> if not found.
 	 *
 	 * @param modelPackage The modelPackage to search
 	 * @param name the name to find.
 	 * @return the found model element.
 	 */
 	public static Object find(org.omg.uml.UmlPackage modelPackage, final String name)
 	{
 		return CollectionUtils.find(
 
 		modelPackage.getModelManagement().getModel().refAllOfType(), new Predicate()
 		{
 
 			public boolean evaluate(Object object)
 			{
 				return ((ModelElement) object).getName().equals(name);
 			}
 		});
 	}
 
 	public static void transform(String sourceXmiFileName, String targetXmiFileName,
 			InputStream xsltFileStream) throws TransformerException, FileNotFoundException
 	{
 		if (sourceXmiFileName != null)
 		{
 			File sourceXmiFile = new File(sourceXmiFileName);
 
 			Source xmlSource = new StreamSource(sourceXmiFile);
 			Source xsltSource = new StreamSource(xsltFileStream);
 			FileOutputStream targetFile = new FileOutputStream(targetXmiFileName);
 			Result result = new StreamResult(targetFile);
 			//create an instance of TransformerFactory 
 			TransformerFactory transFact = TransformerFactory.newInstance();
 			if ((transFact != null) && (xsltSource != null) && (xmlSource != null))
 			{
 
 				Transformer trans = transFact.newTransformer(xsltSource);
 				System.out.println("Transforming");
 				trans.transform(xmlSource, result);
 				System.out.println("Done");
 			}
 		}
 
 	}
 
 	/**
 	 * This method gets the super class for the given UmlClass
 	 * @param klass
 	 * @return
 	 */
 	public static UmlClass getSuperClass(UmlClass klass)
 	{
 		UmlClass superClass = null;
 		List superClasses = getSuperClasses(klass);
 		if (!superClasses.isEmpty())
 		{
 			superClass = (UmlClass) superClasses.iterator().next();
 		}
 		return superClass;
 	}
 
 	/**
 	 * This method gets all super classes for the given UmlClass
 	 * @param klass
 	 * @return
 	 */
 	public static List getSuperClasses(UmlClass klass)
 	{
 		List superClasses = new ArrayList();
 		for (Iterator i = klass.getGeneralization().iterator(); i.hasNext();)
 		{
 			superClasses.add(((Generalization) i.next()).getParent());
 		}
 		return superClasses;
 	}
 
 	/**
 	 * This method deletes unwanted repository files 
 	 */
 	public static void cleanUpRepository()
 	{
 		if (new File("mdr.btd").exists())
 		{
 			(new File("mdr.btd")).delete();
 		}
 		if (new File("mdr.btx").exists())
 		{
 			(new File("mdr.btx")).delete();
 		}
 	}
 
 }
