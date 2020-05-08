 /* 
 * Copyright (C) allesklar.com AG
 * All rights reserved.
 *
 * Author: juergi
 * Date: 27.05.12 
 *
 */
 
 
 package com.jmelzer.data.model;
 
 import javax.persistence.*;
 import java.io.Serializable;
 
 /**
 * Mit den dynamischen Properties knnen Attributerweiterung bei Custimisierungen vermieden werden.
  * Hier wird statt einem Feld eine Assoziation benutzt.
 * In dieser Klasse wird der Typ, Wert und name gespeichert, damit die entsprecheden Konvertierungen gemacht werden knnen.
  *
  * @author J. Melzer
  */
 @Entity
 @Table(name = "dynamic_property")
 @Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
 public class DynamicProperty extends ModelBase implements Serializable {
 
     private static final long serialVersionUID = 2507548174293614228L;
 
     /** Name des Properties. */
     String name;
     /** Typ siehe {@link #INTEGER} etc. */
     Integer type;
     /** Wert als String. */
     String value;
 
     ModelBase source;
 
    /** Konstante fr den Typ, siehe {@link #type}. */
     public static final int INTEGER = 1;
    /** Konstante fr den Typ, siehe {@link #type}. */
     public static final int STRING = 2;
 
     /** default constructor. */
     public DynamicProperty() {
     }
 
     /**
      * Konstruktor mit den wichtigen Werten.
      *
      * @param name  Name
      * @param type  Typ
      * @param value Wert
      */
     public DynamicProperty(String name, int type, String value) {
         this.name = name;
         this.value = value;
         this.type = type;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public Integer getType() {
         return type;
     }
 
     public void setType(Integer type) {
         this.type = type;
     }
 
     public String getValue() {
         return value;
     }
 
     @Transient
     public int getIntValue() {
         return Integer.valueOf(value);
     }
 
     public void setValue(String value) {
         this.value = value;
     }
 
     @ManyToOne
     public ModelBase getSource() {
         return source;
     }
 
     public void setSource(ModelBase source) {
         this.source = source;
     }
 }
