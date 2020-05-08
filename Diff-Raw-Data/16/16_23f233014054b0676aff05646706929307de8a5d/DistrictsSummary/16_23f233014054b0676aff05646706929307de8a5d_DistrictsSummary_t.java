 package ru.yandex.hackaton.server.db.model;
 
 import javax.persistence.*;
 import java.math.BigDecimal;
 
 /**
  * @author Sergey Polovko
  */
 @Entity
 @Table(name = "districts_summary")
 public class DistrictsSummary extends BaseModel<Integer> {
 
     @Id
     @Column(name = "districtid")
     private Integer id;
 
     @Column
     private Integer child_polyclinic;
 
     @Column
     private Integer child_teeth_polyclinic;
 
     @Column
     private Integer city_polyclinic;
 
     @Column
     private Integer dispensary;
 
     @Column
     private Integer drugs;
 
     @Column
     private Integer elementaries;
 
     @Column
     private Integer hospitals;
 
     @Column
     private Integer night_schools;
 
     @Column
     private Integer parks;
 
     @Column
     private Integer pre_schools;
 
     @Column
     private Integer fountains;
 
     @Column
     private Integer wifi;
 
     @Column
     private Integer shops;
 
     @Column
     private Integer high_schools;
 
     @Column
     private Integer piknik;
 
     @Column
     private Integer bus_stops;
 
     @Column
     private Integer libraries;
 
     @Column
     private BigDecimal summ;
 
     @Column
     private Integer size;
 
     public Integer getId() {
         return id;
     }
 
     public void setId(Integer id) {
         this.id = id;
     }
 
     public Integer getDistrictid() {
         return id;
     }
 
     public void setDistrictid(Integer id) {
         this.id = id;
     }
 
     public Integer getChild_polyclinic() {
         return child_polyclinic;
     }
 
     public void setChild_polyclinic(Integer child_polyclinic) {
         this.child_polyclinic = child_polyclinic;
     }
 
     public Integer getChild_teeth_polyclinic() {
         return child_teeth_polyclinic;
     }
 
     public void setChild_teeth_polyclinic(Integer child_teeth_polyclinic) {
         this.child_teeth_polyclinic = child_teeth_polyclinic;
     }
 
     public Integer getCity_polyclinic() {
         return city_polyclinic;
     }
 
     public void setCity_polyclinic(Integer city_polyclinic) {
         this.city_polyclinic = city_polyclinic;
     }
 
     public Integer getDispensary() {
         return dispensary;
     }
 
     public void setDispensary(Integer dispensary) {
         this.dispensary = dispensary;
     }
 
     public Integer getDrugs() {
         return drugs;
     }
 
     public void setDrugs(Integer drugs) {
         this.drugs = drugs;
     }
 
     public Integer getElementaries() {
         return elementaries;
     }
 
     public void setElementaries(Integer elementaries) {
         this.elementaries = elementaries;
     }
 
     public Integer getHospitals() {
         return hospitals;
     }
 
     public void setHospitals(Integer hospitals) {
         this.hospitals = hospitals;
     }
 
     public Integer getNight_schools() {
         return night_schools;
     }
 
     public void setNight_schools(Integer night_schools) {
         this.night_schools = night_schools;
     }
 
     public Integer getParks() {
         return parks;
     }
 
     public void setParks(Integer parks) {
         this.parks = parks;
     }
 
     public Integer getPre_schools() {
         return pre_schools;
     }
 
     public void setPre_schools(Integer pre_schools) {
         this.pre_schools = pre_schools;
     }
 
     public Integer getFountains() {
         return fountains;
     }
 
     public void setFountains(Integer fountains) {
         this.fountains = fountains;
     }
 
     public Integer getWifi() {
         return wifi;
     }
 
     public void setWifi(Integer wifi) {
         this.wifi = wifi;
     }
 
     public Integer getShops() {
         return shops;
     }
 
     public void setShops(Integer shops) {
         this.shops = shops;
     }
 
     public Integer getHigh_schools() {
         return high_schools;
     }
 
     public void setHigh_schools(Integer high_schools) {
         this.high_schools = high_schools;
     }
 
     public Integer getPiknik() {
         return piknik;
     }
 
     public void setPiknik(Integer piknik) {
         this.piknik = piknik;
     }
 
     public BigDecimal getSumm() {
         return summ;
     }
 
     public void setSumm(BigDecimal summ) {
         this.summ = summ;
     }
 
     public Integer getSize() {
         return size;
     }
 
     public void setSize(Integer size) {
         this.size = size;
     }

    public Integer getBus_stops() {
        return bus_stops;
    }

    public void setBus_stops(Integer bus_stops) {
        this.bus_stops = bus_stops;
    }

    public Integer getLibraries() {
        return libraries;
    }

    public void setLibraries(Integer libraries) {
        this.libraries = libraries;
    }
 }
