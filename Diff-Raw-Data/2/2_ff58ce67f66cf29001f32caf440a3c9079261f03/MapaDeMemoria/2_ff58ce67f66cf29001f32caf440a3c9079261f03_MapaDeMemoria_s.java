 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.isolux.properties;
 
 /**
  * Clase que representa las constantes del mapa de memoria de la aplicación.
  * Aquí se encuentran todos los valores enteros que están consignados en el mapa
  * de memoria. Son valores estáticos para ser accesados desde el contexto de la
  * aplicación
  *
  * @author Juan Camilo Cañas Gómez
  *
  */
 public final class MapaDeMemoria {
 
     public static int getBALASTO_OFFSET_NUMERO() {
         return BALASTO_OFFSET_NUMERO;
     }
 
     public static int getBALASTO_NIVEL() {
         return BALASTO_NIVEL;
     }
 
     public static int getBALASTO_NUMB() {
         return BALASTO_NUMB;
     }
 
     public static int getBALASTO_DIRB() {
         return BALASTO_DIRB;
     }
 
     public static int getBALASTO_OFFSET_NIVELES_ESCENAS() {
         return BALASTO_OFFSET_NIVELES_ESCENAS;
     }
 
     public static int getBALASTO_MEMORY_SIZE() {
         return BALASTO_MEMORY_SIZE;
     }
 
     public static int getREINTENTOS() {
         return REINTENTOS;
     }
 
     public static int getDELAY_OPERACIONES_CORTO() {
         return DELAY_OPERACIONES_CORTO;
     }
 
     public static int getDELAY_OPERACIONES_LARGO() {
         return DELAY_OPERACIONES_LARGO;
     }
 
     public static int getBALASTO_DE_FABRICA() {
         return BALASTO_DE_FABRICA;
     }
 
     public static String getCONFIGURACION_GENERAL_IP_GENERAL() {
         return CONFIGURACION_GENERAL_IP_GENERAL;
     }
 
     public static String getCONFIGURACION_GENERAL_IP_MASCARA() {
         return CONFIGURACION_GENERAL_IP_MASCARA;
     }
 
     public static String getCONFIGURACION_GENERAL_IP_GATEWAY() {
         return CONFIGURACION_GENERAL_IP_GATEWAY;
     }
 
     public static int getCONFIGURACION_GENERAL_PUERTO_GENERAL() {
         return CONFIGURACION_GENERAL_PUERTO_GENERAL;
     }
 
     public static int getCONFIGURACION_GENERAL_BITS_NOMBRE() {
         return CONFIGURACION_GENERAL_BITS_NOMBRE;
     }
 
     public static int getCONFIGURACION_GENERAL_PUERTO_MAXIMONUM() {
         return CONFIGURACION_GENERAL_PUERTO_MAXIMONUM;
     }
 
     public MapaDeMemoria() {
     }
     //<editor-fold defaultstate="collapsed" desc="Balastos Mapa de memoria">
     private static int BALASTO_OFFSET_NUMERO = Integer.parseInt(PropHandler.getProperty("balast.init.position"));
     private static int BALASTO_NIVEL = Integer.parseInt(PropHandler.getProperty("balast.memory.levelOffset"));
     private static int BALASTO_NUMB = Integer.parseInt(PropHandler.getProperty("balast.memory.numb"));
     private static int BALASTO_DIRB = Integer.parseInt(PropHandler.getProperty("balast.memory.dirb"));
 //    public final c int BALASTO_NIVEL=Integer.parseInt(PropHandler.getProperty("balast.memory.levelOffset"));
     private static int BALASTO_OFFSET_NIVELES_ESCENAS = Integer.parseInt(PropHandler.getProperty("balast.memory.valorescenas.memoryoffset"));
     private static int BALASTO_MEMORY_SIZE = Integer.parseInt(PropHandler.getProperty("balast.memory.size"));
     //</editor-fold>
     //<editor-fold defaultstate="collapsed" desc="Miscelaneas">
    private static final int REINTENTOS = 5;
     /**
      * Retardo que se inserta entre las operaciones de escritura y lectura de
      * registros.
      */
     private static int DELAY_OPERACIONES_CORTO = 500;
     private static int DELAY_OPERACIONES_LARGO = 1500;
     private static int BALASTO_DE_FABRICA = Integer.parseInt(PropHandler.getProperty("balast.config.defabrica"));
     //</editor-fold>
     //<editor-fold defaultstate="collapsed" desc="Configuracion general">
     /**
      *
      */
     private static String CONFIGURACION_GENERAL_IP_GENERAL = String.valueOf(PropHandler.getProperty("general.ip"));
     private static String CONFIGURACION_GENERAL_IP_MASCARA = String.valueOf(PropHandler.getProperty("general.ip.mask"));
     private static String CONFIGURACION_GENERAL_IP_GATEWAY = String.valueOf(PropHandler.getProperty("general.ip.gateway"));
     private static int CONFIGURACION_GENERAL_PUERTO_GENERAL = Integer.parseInt(PropHandler.getProperty("general.port"));
     private static int CONFIGURACION_GENERAL_BITS_NOMBRE = Integer.parseInt(PropHandler.getProperty("general.name.bytes"));
     private static int CONFIGURACION_GENERAL_PUERTO_MAXIMONUM = Integer.parseInt(PropHandler.getProperty("general.offset.max"));
     //</editor-fold>
 }
