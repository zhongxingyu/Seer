 package com.ibm.watson.smapi;
 
 import java.io.File;
 import java.io.FileOutputStream;
import com.ibm.wala.shrikeBT.shrikeCT.ClassInstrumenter;
import com.ibm.wala.shrikeBT.shrikeCT.OfflineInstrumenter;
import com.ibm.wala.shrikeCT.ClassReader;
import com.ibm.wala.shrikeCT.ClassWriter;
import com.ibm.wala.shrikeCT.SourceDebugExtensionWriter;
import com.ibm.wala.shrikeCT.ClassReader.AttrIterator;
 
 public class Main {
     	public static boolean debug= false;
 
     	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		String MAIN_CLASS = args[0];
 		smapify(MAIN_CLASS, null, null);
 	}
 	
 	/*
 	 * filename: the name of the file, possibly with path information
 	 * relPathPrefix: the portion of the filename we wish to conserve
 	 * outputfile: the output filename
 	 * e.g.:
 	 * filename: C:/foo/bar/bla.x
 	 * relPathPrefix: bar  
 	 * ouputfile: C:/foo/bin/bar/bla.class
 	 * information in SMAP becomes: bar/bla.x
 	 */
 	public static void smapify(String filename, String relPathPrefix, String outputfile){
 		String prefix = removeExt(filename);
 		String origExten = filename.substring(filename.lastIndexOf('.')+1);
 
 		if (debug) {
 			System.out.println("origExten=" + origExten);
 			System.out.println("smapify filename: " + filename);
 			System.out.println("with pathPrefix: " + relPathPrefix);
 			System.out.println("and outputfile: " + outputfile);
 		}
 		
 		LineMapBuilder lmb = new LineMapBuilder(prefix);
 		String smap = SmapBuilder.get(prefix, relPathPrefix, lmb.get(), origExten);
 
 		if (debug)
 			System.out.println(smap);
 		
 		try {
 			OfflineInstrumenter oi = new OfflineInstrumenter();
 			String inputName = null;
 			if (outputfile == null)
 				inputName = prefix + ".class";
 			else 
 				inputName = outputfile;
 			File input = new File(inputName);
 			oi.addInputClass(input);
 			oi.beginTraversal();
 			ClassInstrumenter ci = oi.nextClass();
 			ClassReader cr = ci.getReader();
 			ClassWriter w = new ClassWriter();
 			w.setRawCP(cr.getCP(), true);
 			w.setMajorVersion(cr.getMajorVersion());
 			w.setMinorVersion(cr.getMinorVersion());
 			w.setAccessFlags(cr.getAccessFlags());
 			w.setName(cr.getName());
 			w.setSuperName(cr.getSuperName());
 			w.setInterfaceNames(cr.getInterfaceNames());
 	     
 			ClassReader.AttrIterator iter = new ClassReader.AttrIterator();
 			
 		
 			int fieldCount = cr.getFieldCount();	
 			for (int i = 0; i < fieldCount; i++) {
 				cr.initFieldAttributeIterator(i, iter);
 				w.addField(cr.getFieldAccessFlags(i), cr.getFieldName(i), cr.getFieldType(i), collectAttributes(cr, iter)); 
 			}
 
 			int methodCount = cr.getMethodCount();
 			for (int i = 0; i < methodCount; i++) {
 				cr.initMethodAttributeIterator(i, iter);
 				w.addMethod(cr.getMethodAccessFlags(i), cr.getMethodName(i), cr.getMethodType(i), collectAttributes(cr, iter));
 			}
 
 			cr.initClassAttributeIterator(iter);
 			for (; iter.isValid(); iter.advance()) {
 				w.addClassAttribute(getRawAttribute(cr, iter));
 			}
 	    
 			SourceDebugExtensionWriter sw = new SourceDebugExtensionWriter(w);
 			sw.setDebugInfo(smap);
 			w.addClassAttribute(sw);
 			FileOutputStream fw = new FileOutputStream(new File(inputName));
 			fw.write(w.makeBytes());
 			fw.close();
 			oi.close();
 			
 			
 			
 		} catch (Exception e){
 			System.err.println(e);
 		}
 		
 	}
 	
 	private static String removeExt(String filename){
 		int i = filename.lastIndexOf(".");
 		return filename.substring(0,i);
 	}
 	
 	private static ClassWriter.Element[] collectAttributes(ClassReader cr, AttrIterator iter) throws Exception {
 		ClassWriter.Element[] elems = new ClassWriter.Element[iter.getRemainingAttributesCount()];
 		for (int i = 0; i < elems.length; i++) {			
 		    elems[i] = getRawAttribute(cr, iter);
 			iter.advance();
 		}
 		return elems;
 	}
 	
 	private static ClassWriter.Element getRawAttribute(ClassReader cr, AttrIterator iter) {
 		int offset = iter.getRawOffset();
 		int end = offset + iter.getRawSize();
 		return new ClassWriter.RawElement(cr.getBytes(), offset, end - offset);
 	}
 }
