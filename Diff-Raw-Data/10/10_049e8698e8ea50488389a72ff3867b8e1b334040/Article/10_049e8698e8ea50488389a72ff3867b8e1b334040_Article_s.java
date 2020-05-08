 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package database.tables;
 
 import database.extra.Record;
 import database.models.ArticleModel;
 import tools.Configuration;
 
 /**
  *
  * @author Robin jr
  */
 public class Article extends Record{
     private String code;
     private String name;
     private double priceA;
     private double priceB;
     private String unit;
     private double taxes;
 
     private Article(int id, String code, String name, double priceA, double priceB, String unit, double taxes) {
 	super(id, Configuration.center().getDB_TABLE_ART());
 	this.code = code;
 	this.name = name;
 	this.priceA = priceA;
 	this.priceB = priceB;
 	this.unit = unit;
 	this.taxes = taxes;
     }
     
     private Article(int id, ArticleModel model){
 	super(id, Configuration.center().getDB_TABLE_ART());
 	this.code = model.getCode();
 	this.name = model.getName();
 	this.priceA = model.getPriceA();
 	this.priceB = model.getPriceB();
 	this.unit = model.getUnit();
 	this.taxes = model.getTaxes();
     }
     
     public Article(Article copy){
 	super(copy.getPrimaryKeyValue(), copy.getTableName());
 	this.code = copy.getCode();
 	this.name = copy.getName();
 	this.priceA = copy.getPriceA();
 	this.priceB = copy.getPriceB();
 	this.unit = copy.getUnit();
 	this.taxes = copy.getTaxes();
     }
     
     public void copy(Article copy){
 	this.code = copy.getCode();
 	this.name = copy.getName();
 	this.priceA = copy.getPriceA();
 	this.priceB = copy.getPriceB();
 	this.unit = copy.getUnit();
 	this.taxes = copy.getTaxes();
     }
     
     public static Article loadWithValues(int id, String code, String name, double priceA, double priceB, String unit, double taxes){
 	return new Article(id, code, name, priceA, priceB, unit, taxes);
     }
     
     public static Article createFromModel(int id, ArticleModel model){
 	return new Article(id, model);
     }
 
     public String getCode() {
 	return code;
     }
 
     public void setCode(String code) {
 	this.code = code;
     }
 
     public String getName() {
 	return name;
     }
 
     public void setName(String name) {
 	this.name = name;
     }
 
     public double getPriceA() {
 	return priceA;
     }
 
     public void setPriceA(double priceA) {
 	this.priceA = priceA;
     }
 
     public double getPriceB() {
 	return priceB;
     }
 
     public void setPriceB(double priceB) {
 	this.priceB = priceB;
     }
 
     public String getUnit() {
 	return unit;
     }
 
     public void setUnit(String unit) {
 	this.unit = unit;
     }
 
     public double getTaxes() {
 	return taxes;
     }
 
     public void setTaxes(double taxes) {
 	this.taxes = taxes;
     }
     
     @Override
     public String toString(){
 //	return name+" ("+code+")";
 	return name;
     }
 
     @Override
     public boolean update() {
	throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public boolean delete() {
 	throw new UnsupportedOperationException("Not supported yet.");
     }
 }
