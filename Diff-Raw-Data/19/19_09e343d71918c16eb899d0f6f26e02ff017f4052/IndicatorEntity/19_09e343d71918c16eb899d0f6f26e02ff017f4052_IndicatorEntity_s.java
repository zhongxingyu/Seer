 package org.motechproject.carereporting.domain;
 
 import org.codehaus.jackson.map.annotate.JsonView;
 import org.hibernate.annotations.FetchMode;
 import org.hibernate.annotations.FetchProfile;
 import org.hibernate.validator.constraints.NotEmpty;
 import org.motechproject.carereporting.domain.views.BaseView;
 import org.motechproject.carereporting.domain.views.DashboardJsonView;
 import org.motechproject.carereporting.domain.views.IndicatorJsonView;
 
 import javax.persistence.AttributeOverride;
 import javax.persistence.AttributeOverrides;
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 import javax.validation.constraints.NotNull;
 import java.math.BigDecimal;
 import java.util.Set;
 
 @SuppressWarnings("PMD.AvoidDuplicateLiterals")
 @Entity
 @Table(name = "indicator")
 @AttributeOverrides({
         @AttributeOverride(name = "id", column = @Column(name = "indicator_id"))
 })
 @FetchProfile(name = "indicator-with-reports", fetchOverrides = {
         @FetchProfile.FetchOverride(entity = IndicatorEntity.class, association = "reports", mode = FetchMode.JOIN)
 })
 public class IndicatorEntity extends AbstractEntity {
 
     @ManyToOne(cascade = CascadeType.ALL)
     @JoinColumn(name = "denominator_id", referencedColumnName = "dw_query_id")
     private DwQueryEntity denominator;
 
     @NotNull
     @ManyToOne(cascade = CascadeType.ALL)
     @JoinColumn(name = "numerator_id", referencedColumnName = "dw_query_id")
     private DwQueryEntity numerator;
 
     @NotNull
     @ManyToMany(fetch = FetchType.EAGER)
     @JoinTable(name = "indicator_indicator_category", joinColumns = { @JoinColumn(name = "indicator_id") },
             inverseJoinColumns = { @JoinColumn(name = "indicator_category_id") })
     @JsonView({ IndicatorJsonView.IndicatorModificationDetails.class })
     private Set<IndicatorCategoryEntity> categories;
 
     @NotNull
     @ManyToOne
     @JoinColumn(name = "area_id", referencedColumnName = "area_id", nullable = false)
     @JsonView({ IndicatorJsonView.IndicatorModificationDetails.class, DashboardJsonView.class })
     private AreaEntity area;
 
     @ManyToOne
     @JoinColumn(name = "user_id", referencedColumnName = "user_id")
     private UserEntity owner;
 
     @ManyToMany
     @JoinTable(name = "indicator_role", joinColumns = { @JoinColumn(name = "indicator_id") },
             inverseJoinColumns = { @JoinColumn(name = "role_id") })
     private Set<RoleEntity> roles;
 
     @OneToMany(mappedBy = "indicator", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
     @JsonView({ IndicatorJsonView.IndicatorDetails.class, IndicatorJsonView.IndicatorModificationDetails.class, DashboardJsonView.class })
     private Set<ReportEntity> reports;
 
     @OneToMany(mappedBy = "indicator", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
     private Set<IndicatorValueEntity> indicatorValues;
 
     @NotNull
     @ManyToOne
     @JoinColumn(name = "frequency_id", nullable = false)
     @JsonView({ IndicatorJsonView.IndicatorModificationDetails.class, DashboardJsonView.class })
     private FrequencyEntity defaultFrequency;
 
     @NotNull
     @NotEmpty
     @Column(name = "name", unique = true, nullable = false)
     @JsonView({ BaseView.class })
     private String name;
 
     @Column(name = "trend", nullable = true)
     @JsonView({ BaseView.class })
     private BigDecimal trend;
 
     @Column(name = "is_computing", nullable = false)
    @JsonView({ DashboardJsonView.class })
     private Boolean isComputing;
 
     @Column(name = "is_additive", nullable = false)
     @JsonView({ DashboardJsonView.class })
     private Boolean isAdditive;
 
     public Boolean getAdditive() {
         return isAdditive;
     }
 
     public void setAdditive(Boolean additive) {
         isAdditive = additive;
     }
 
     public Boolean getComputing() {
         return isComputing;
     }
 
     public void setComputing(Boolean computing) {
         isComputing = computing;
     }
 
     public DwQueryEntity getDenominator() {
         return denominator;
     }
 
     public void setDenominator(DwQueryEntity denominator) {
         this.denominator = denominator;
     }
 
     public DwQueryEntity getNumerator() {
         return numerator;
     }
 
     public void setNumerator(DwQueryEntity numerator) {
         this.numerator = numerator;
     }
 
     public Set<IndicatorCategoryEntity> getCategories() {
         return categories;
     }
 
     public void setCategories(Set<IndicatorCategoryEntity> categories) {
         this.categories = categories;
     }
 
     public AreaEntity getArea() {
         return area;
     }
 
     public void setArea(AreaEntity area) {
         this.area = area;
     }
 
     public Set<ReportEntity> getReports() {
         return reports;
     }
 
     public void setReports(Set<ReportEntity> reports) {
         this.reports = reports;
     }
 
     public FrequencyEntity getDefaultFrequency() {
         return defaultFrequency;
     }
 
     public void setDefaultFrequency(FrequencyEntity defaultFrequency) {
         this.defaultFrequency = defaultFrequency;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public Set<IndicatorValueEntity> getIndicatorValues() {
         return indicatorValues;
     }
 
     public void setIndicatorValues(Set<IndicatorValueEntity> indicatorValues) {
         this.indicatorValues = indicatorValues;
     }
 
     public BigDecimal getTrend() {
         return trend;
     }
 
     public void setTrend(BigDecimal trend) {
         this.trend = trend;
     }
 
     public UserEntity getOwner() {
         return owner;
     }
 
     public void setOwner(UserEntity owner) {
         this.owner = owner;
     }
 
     public Set<RoleEntity> getRoles() {
         return roles;
     }
 
     public void setRoles(Set<RoleEntity> roles) {
         this.roles = roles;
     }
 }
