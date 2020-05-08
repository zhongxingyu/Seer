 package ngmep.ngmep.datamodel;
 /*
 	ngmep2osm - importador de datos de ngmep a openstreetmap
 	
 	Copyright (C) 2011-2012 Alberto Fernández <infjaf@gmail.com>
 	
 	This program is free software: you can redistribute it and/or modify
 	it under the terms of the GNU General Public License as published by
 	the Free Software Foundation, either version 3 of the License, or
 	(at your option) any later version.
 	
 	This program is distributed in the hope that it will be useful,
 	but WITHOUT ANY WARRANTY; without even the implied warranty of
 	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 	GNU General Public License for more details.
 	
 	You should have received a copy of the GNU General Public License
 	along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
 
public class Entidad {
     private String codine;
     private String codineMun;
     private String nombreMun;
     private int administrativeLevel; //NOPMD
     private String name;
     private double altura;
     private String sourceAltura;
     private double lon;
     private double lat;
     private long osmid;
     private double poblacion;
     private double poblacionMuni;
     private String place;
     private String codigoProvincia;
     private String nombreOficial;
     private String nombreAlternativo;
     private String nombreAntiguo;
     private String name1;
     private String name2;
     private String lan1;
     private String lan2;
     private String fechaCambioNombre;
     private String decreto;
     private String locName;
     private int estadoManual;
     private int estadoRobot;
     private String decisionNombre;
     
     public Entidad (){
         super();
     }
 
     public String getCodine() {
         return codine;
     }
 
     public void setCodine(final String codine) {
         this.codine = codine;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(final String name) {
         this.name = name;
     }
 
     public double getAltura() {
         return altura;
     }
 
     public void setAltura(final double altura) {
         this.altura = altura;
     }
 
     public String getSourceAltura() {
         return sourceAltura;
     }
 
     public void setSourceAltura(final String sourceAltura) {
         this.sourceAltura = sourceAltura;
     }
 
     public double getLon() {
         return lon;
     }
 
     public void setLon(final double lon) {
         this.lon = lon;
     }
 
     public double getLat() {
         return lat;
     }
 
     public void setLat(final double lat) {
         this.lat = lat;
     }
 
     public long getOsmid() {
         return osmid;
     }
 
     public void setOsmid(final long osmid) {
         this.osmid = osmid;
     }
 
     public double getPoblacion() {
         return poblacion;
     }
 
     public void setPoblacion(final double poblacion) {
         this.poblacion = poblacion;
     }
 
     public String getPlace() {
         return place;
     }
 
     public void setPlace(final String place) {
         this.place = place;
     }
 
     public String getCodigoProvincia() {
         return codigoProvincia;
     }
 
     public void setCodigoProvincia(final String codigoProvincia) {
         this.codigoProvincia = codigoProvincia;
     }
 
     public String getNombreOficial() {
         return nombreOficial;
     }
 
     public void setNombreOficial(final String nombreOficial) {
         this.nombreOficial = nombreOficial;
     }
 
     public String getNombreAlternativo() {
         return nombreAlternativo;
     }
 
     public void setNombreAlternativo(final String nombreAlternativo) {
         this.nombreAlternativo = nombreAlternativo;
     }
 
     public String getNombreAntiguo() {
         return nombreAntiguo;
     }
 
     public void setNombreAntiguo(final String nombreAntiguo) {
         this.nombreAntiguo = nombreAntiguo;
     }
 
     public String getName1() {
         return name1;
     }
 
     public void setName1(final String name1) {
         this.name1 = name1;
     }
 
     public String getName2() {
         return name2;
     }
 
     public void setName2(final String name2) {
         this.name2 = name2;
     }
 
     public String getLan1() {
         return lan1;
     }
 
     public void setLan1(final String lan1) {
         this.lan1 = lan1;
     }
 
     public String getLan2() {
         return lan2;
     }
 
     public void setLan2(final String lan2) {
         this.lan2 = lan2;
     }
 
     public String getFechaCambioNombre() {
         return fechaCambioNombre;
     }
 
     public void setFechaCambioNombre(final String fechaCambioNombre) {
         this.fechaCambioNombre = fechaCambioNombre;
     }
 
     public String getDecreto() {
         return decreto;
     }
 
     public void setDecreto(final String decreto) {
         this.decreto = decreto;
     }
 
     public String getLocName() {
         return locName;
     }
 
     public void setLocName(final String locName) {
         this.locName = locName;
     }
 
     public String getCodineMun() {
         return codineMun;
     }
 
     public void setCodineMun(final String codineMun) {
         this.codineMun = codineMun;
     }
 
     public String getNombreMun() {
         return nombreMun;
     }
 
     public void setNombreMun(final String nombreMun) {
         this.nombreMun = nombreMun;
     }
 
     public int getAdministrativeLevel() {
         return administrativeLevel;
     }
 
     public void setAdministrativeLevel(final int adminLevel) {
         this.administrativeLevel = adminLevel;
     }
 
     public double getPoblacionMuni() {
         return poblacionMuni;
     }
 
     public void setPoblacionMuni(final double poblacionMuni) {
         this.poblacionMuni = poblacionMuni;
     }
 
     public int getEstadoManual() {
         return estadoManual;
     }
 
     public void setEstadoManual(final int estadoManual) {
         this.estadoManual = estadoManual;
     }
 
     public int getEstadoRobot() {
         return estadoRobot;
     }
 
     public void setEstadoRobot(final int estadoRobot) {
         this.estadoRobot = estadoRobot;
     }
 
     public String getDecisionNombre() {
         return decisionNombre;
     }
 
     public void setDecisionNombre(final String decisionNombre) {
         this.decisionNombre = decisionNombre;
     }
     
     
     
 
 }
 
 
 /*
 
 
 CREATE TABLE nocapitales (
  cod_ine                  character(11)    ,
  name                     text             , 
  altura                   double precision , 
  origen_alturas           character(12)    , 
  the_geom                 geometry         , 
  osmid                    bigint           , 
  poblacion                double precision , 
  place                    text             , 
  cod_prov                 text             , 
  official_name            text             , 
  alt_name                 text             , 
  old_name                 text             , 
  name1                    text             , 
  name2                    text             , 
  lan1                     character(3)     , 
  lan2                     character(3)     , 
  fecha_cambio_nom         character(50)    , 
  decreto_boletin_oficial  character(100)   ,
  loc_name                 text             
 );
 
 
 CREATE TABLE capitales (
  cod_ine_mun              character(11)    ,
  cod_ine          character(11)    , 
  nombre_mun               character(100)   , 
  name                     text             , 
  altura                   double precision , 
  origen_alturas           character(12)    , 
  the_geom                 geometry         , 
  osmid                    bigint           , 
  place                    character(20)    , 
  poblacion                double precision , 
  admin_level              smallint         , 
  cod_prov                 character(2)     , 
  poblacion_muni           double precision , 
  official_name            text             , 
  alt_name                 text             , 
  old_name                 text             , 
  name1                    text             ,
  name2                    text             ,
  lan1                     character(3)     , 
  lan2                     character(3)     , 
  fecha_cambio_nom         character(50)    , 
  decreto_boletin_oficial  character(100)   , 
  loc_name                 text              
 );
 
 
 
 */
