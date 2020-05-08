 ////////////////////////////////////////////////////////////////////////////////
 //
 //  ADOBE SYSTEMS INCORPORATED
 //  Copyright 2007 Adobe Systems Incorporated
 //  All Rights Reserved.
 //
 //  NOTICE: Adobe permits you to use, modify, and distribute this file
 //  in accordance with the terms of the license agreement accompanying it.
 //
 ////////////////////////////////////////////////////////////////////////////////
 
 package macromedia.asc.embedding;
 
 public class ConfigVar 
 {
    public String ns;
    public String name;
    public String value;
     public ConfigVar(String ns, String name, String value)
     {
        this.ns = ns;
        this.name = name;
         this.value = value;
     }
 }
