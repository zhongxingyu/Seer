 package webapp;
 
 import java.util.List;
 import java.util.Map;
 
 import org.codehaus.jackson.annotate.JsonProperty;
 
 /*
  *  Provides annotations for Company class used for reading from CrunchBase JSON
  *  I decided to be explicit with my getters/setters and disable Jackson auto-detection,
  *  so you need to put a getter and setter marked as @JsonProperty for each field
  */
 public abstract class CompanyMixIn extends Company {
 	@JsonProperty("name") public abstract String getName();
 	@JsonProperty("permalink") public abstract String getPermalink();
 	@JsonProperty("homepage_url") public abstract String getHomepageUrl();
 	@JsonProperty("blog_url") public abstract String getBlogUrl();
 	@JsonProperty("overview") public abstract String getOverview();
 	@JsonProperty("number_of_employees") public abstract int getNumEmployees();
 	@JsonProperty("total_money_raised") public abstract double getTotalMoneyRaised();
 	@JsonProperty("funding_rounds") public abstract List<FundingRound> getFundingRounds();
 	@JsonProperty("image") public abstract String getImageUrl();
 	@JsonProperty("category_code") public abstract String getIndustry();
 	@JsonProperty("offices") public abstract List<Office> getOffices();
 	@JsonProperty("founded_year") public abstract int getYearFounded();
 	@JsonProperty("competitions") public abstract List<String> getCompetitors();
 	
 	@JsonProperty("name") protected abstract void setName(String name);
 	@JsonProperty("permalink") protected abstract void setPermalink(String permalink);
 	@JsonProperty("homepage_url") protected abstract void setHomepageUrl(String url);
 	@JsonProperty("blog_url") protected abstract void setBlogUrl(String url);
	@JsonProperty("overview") protected abstract void setOverview(String overview);
 	@JsonProperty("number_of_employees") protected abstract void setNumEmployees(int n);
 	@JsonProperty("total_money_raised") protected abstract void setTotalMoneyRaised(String totalMoney);
 	@JsonProperty("funding_rounds") protected abstract void setFundingRounds(List<FundingRound> rounds);
 	@JsonProperty("image") protected abstract void setImageUrl(Map<String, List<List<Object>>> availableUrls);
 	@JsonProperty("category_code") protected abstract void setIndustry(String industry);
 	@JsonProperty("offices") protected abstract void setOffices(List<Office> offices);
 	@JsonProperty("founded_year") protected abstract void setYearFounded(int year);
 	@JsonProperty("competitions") protected abstract void setCompetitors(List<Map<String, Map<String, Object>>> json);
 }
