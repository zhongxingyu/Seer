 package com.megalogika.sv.service;
 
 import java.io.Serializable;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Component;
 import org.springframework.util.StringUtils;
 
 import com.megalogika.sv.model.E;
 import com.megalogika.sv.model.ProductCategory;
 import com.megalogika.sv.service.filter.ApprovedContentFilter;
 import com.megalogika.sv.service.filter.ApprovedProductFilter;
 import com.megalogika.sv.service.filter.CategoryFilter;
 import com.megalogika.sv.service.filter.CompanyFilter;
 import com.megalogika.sv.service.filter.EFilter;
 import com.megalogika.sv.service.filter.Filter;
 import com.megalogika.sv.service.filter.GmoFilter;
 import com.megalogika.sv.service.filter.HazardFilter;
 import com.megalogika.sv.service.filter.KeywordFilter;
 import com.megalogika.sv.service.filter.RankingFilter;
 import com.megalogika.sv.service.filter.RankingFilterException;
 import com.megalogika.sv.service.filter.TextKeywordFilter;
 import com.megalogika.sv.service.filter.TextNameKeywordFilter;
 import com.megalogika.sv.service.filter.UnapprovedProductFilter;
 
 @Component("productSearchCriteria")
 @Scope("session")
 public class ProductSearchCriteria extends SearchCriteria implements Serializable {
 	private static final long serialVersionUID = -5502280523848665622L;
 	public final static String ORDER_BY_HAZARD = "hazard";
 	public final static String ORDER_BY_PRODUCER = "producer";
 	public final static String ORDER_BY_HAZARD_ASC = "hazard asc";
 	
 	private String q = "";
 	private String queryString;
 
 	
 	
 	public ProductSearchCriteria() {
 		super();
 	}
 
 	@Override
 	public String getOrderByClause() {
 		String ret = "";
 
 //		if (hasAuthority(User.ROLE_ADMIN)) {
 //			ret += "p.approved asc, ";
 //		}
 
 		if (ORDER_BY_DATE.equals(orderBy)) {
 			ret += "p.entryDate desc";
 		} else if (ORDER_BY_HAZARD.equals(orderBy)) {
 			ret += "p.hazard desc";
 		} else if (ORDER_BY_HAZARD_ASC.equals(orderBy)) {
 			ret += "p.hazard asc";			
 		} else if (ORDER_BY_PRODUCER.equals(orderBy)) {
 			ret += "lower(trim(BOTH ' \"' FROM p.company)) asc"; //TODO dar kazkaip apostrofa paescapinti
 		} else {
 			ret += "p.entryDate desc";
 		}
 
 		return ret;
 	}
 
 	/**
 	 * @param eid -
 	 *            konservanto id
 	 * @param name -
 	 *            konservanto numeris
 	 */
 	public void addEFilter(long eid, String name) {
 		setPage(0);
 		for (int i = 0; i < filters.size(); i++) {
 			if ((filters.get(i) instanceof HazardFilter) ||
 				(filters.get(i) instanceof EFilter ||
				(filters.get(i) instanceof TextKeywordFilter)))  
 			{
 				filters.remove(i);
 			}
 		}
 		addFilter(new EFilter(eid, name, filters.isEmpty()));
 	}
 
 	public void addCategoryFilter(ProductCategory category) {
 		setPage(0);
 		CategoryFilter filter = new CategoryFilter(category, filters.isEmpty());
 		for (int i = 0; i < filters.size(); i++) {
 			if (filters.get(i) instanceof CategoryFilter) {
 				((CategoryFilter) filters.get(i)).setCategory(filter.getCategory());
 			}
			if (filters.get(i) instanceof TextKeywordFilter) {
 				filters.remove(i);
 			}
 		}
 		addFilter(filter);
 	}
 
 	public void addCompanyFilter(String company) {
 		setPage(0);
 		for (int i = 0; i < filters.size(); i++) {
 			if (filters.get(i) instanceof TextKeywordFilter) {
 				filters.remove(i);
 			}
 		}
 		
 		addFilter(new CompanyFilter(company, filters.isEmpty()));
 	}
 
 	public void addHazardFilter(String hazard, EService eService) {
 		setPage(0);
 		for (int i = 0; i < filters.size(); i++) {
 			if ((filters.get(i) instanceof HazardFilter) || 
 				(filters.get(i) instanceof EFilter) ||
 				(filters.get(i) instanceof TextKeywordFilter)) 
 			{
 				filters.remove(i);
 			}
 		}
 
 		addFilter(new HazardFilter(hazard, eService, filters.isEmpty()));
 	}
 
 	public void addGmoFilter() {
 		setPage(0);
 		addFilter(new GmoFilter());
 	}
 
 	public void addApprovedContentFilter() {
 		setPage(0);
 		addFilter(new ApprovedContentFilter(true));
 	}
 
 	public void addUnapprovedProductFilter() {
 		setPage(0);
 		List<Filter> fList = this.getFilters();
 		// TODO: move this to new method removeFilter(String name)
 		logger.debug("GOING THROUGH FILTERS... [" + this.getFilters().size() + "]");
 		for (int i = 0; i < this.getFilters().size(); i++) {
 			logger.debug("FILTERS[" + this.getFilters().get(i).getDescriptionArgument() + "]");
 			if (fList.get(i).getDescriptionArgument().equalsIgnoreCase("ApprovedProductFilter")) {
 				logger.debug("REMOVING FILTER: " + fList.get(i).getDescriptionArgument());
 				logger.debug("FILTER INDEX: " + i);
 				this.removeFilter("" + i);
 				break;
 			}
 		}
 		addFilter(new UnapprovedProductFilter(true));
 	}
 	
 	public void addApprovedProductFilter() {
 		setPage(0);
 		addFilter(new ApprovedProductFilter(true));
 	}
 
 	@Override
 	public String getWhereClause() {
 		// if (filters.isEmpty()) {
 		// return "";
 		// }
 
 		StringBuffer ret = new StringBuffer();
 		if (hasFilter()) {
 			if (hasEFilter()) {
 				logger.debug("HAS E FILTER");
 				ret.append(" ,IN(p.conservants) c where (");
 			} else {
 				ret.append(" where (");
 			}
 			for (Filter f : filters) {
 				ret.append(f.getFilterClause());
 			}
 			ret.append(")");
 		}
 
 		return ret.toString();
 	}
 	
 	public boolean hasEFilter() {
 		for (Filter f : getFilters()) {
 			if (f instanceof EFilter) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public void addKeywordFilter(String query) {
 		setPage(0);
 		addFilter(new KeywordFilter(query, filters.isEmpty()));
 	}
 
 	public void addEFilter(E e) {
 		addEFilter(e.getId(), e.getName());
 	}
 
 	public void addTextKeywordFilter(String query) throws RankingFilterException {
 		if (null != getRankingFilter()) {
 			throw new RankingFilterException("RankingFilter jau yra šiuose kriterijuose!");
 		} else {
 			setPage(0);
 			getFilters().clear();
 			addFilter(new TextKeywordFilter(query, filters.isEmpty()));
 		}
 	}
 
 	public void addTextNameKeywordFilter(String query) throws RankingFilterException {
 		if (null != getRankingFilter()) {
 			throw new RankingFilterException("RankingFilter jau yra šiuose kriterijuose!");
 		} else {
 			setPage(0);
 			getFilters().clear();
 			addFilter(new TextNameKeywordFilter(query, filters.isEmpty()));
 		}
 	}
 	
 	public RankingFilter getRankingFilter() {
 		for (Filter f : getFilters()) {
 			if (f instanceof RankingFilter) {
 				return (RankingFilter) f;
 			}
 		}
 		
 		return null;
 	}
 
 	public void setQ(String q) {
 		this.q = q;
 	}
 
 	public String getQ() {
 		return q;
 	}
 	
 	@Override
 	public String toString() {
 		return "PSQ[q="+q+",page="+page+",itemsPerPage="+itemsPerPage+"]";
 	}
 
 	public boolean hasQuery() {
 		return StringUtils.hasText(q);
 	}
 
 	public void setQueryString(String queryString) {
 		this.queryString = queryString;
 	}
 
 	public String getQueryString() {
 		return queryString;
 	}
 	
 }
