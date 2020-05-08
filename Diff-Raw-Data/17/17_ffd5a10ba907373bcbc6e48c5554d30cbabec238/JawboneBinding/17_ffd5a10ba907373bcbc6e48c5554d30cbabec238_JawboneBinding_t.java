 package com.rapportive.jawbone;
 
 import com.sun.jna.Library;
 import com.sun.jna.Native;
 import com.sun.jna.Pointer;
 
 import com.sun.jna.ptr.LongByReference;
 import com.sun.jna.ptr.PointerByReference;
 
 public interface JawboneBinding extends Library {
     JawboneBinding INSTANCE = (JawboneBinding) Native.loadLibrary("wbxml2", JawboneBinding.class);
 
 
     int wbxml_conv_wbxml2xml_create(PointerByReference convPtr);
 
     void wbxml_conv_wbxml2xml_set_gen_type(Pointer conv, int genType);
     void wbxml_conv_wbxml2xml_set_language(Pointer conv, int language);
     void wbxml_conv_wbxml2xml_set_charset(Pointer conv, int charset);
     void wbxml_conv_wbxml2xml_set_indent(Pointer conv, int indent);
     void wbxml_conv_wbxml2xml_enable_preserve_whitespaces(Pointer conv);
 
     int wbxml_conv_wbxml2xml_run(Pointer conv, byte[] wbxml, long wbxmlLength, PointerByReference xmlPtr, LongByReference xmlLength);
 
     void wbxml_conv_wbxml2xml_destroy(Pointer conv);


    String wbxml_errors_string(int ret);
 }
