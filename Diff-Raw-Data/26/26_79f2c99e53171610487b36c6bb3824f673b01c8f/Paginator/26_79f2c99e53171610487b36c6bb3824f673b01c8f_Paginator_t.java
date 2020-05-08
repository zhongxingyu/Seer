 package jipdbs.tag;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import javax.servlet.jsp.JspException;
 import javax.servlet.jsp.JspWriter;
 import javax.servlet.jsp.tagext.TagSupport;
 
 import jipdbs.core.util.Functions;
 
 public class Paginator extends TagSupport {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 9117414369218162114L;
 	
 	private static final int LEADING_PAGE_RANGE_DISPLAYED = 4;
 	private static final int TRAILING_PAGE_RANGE_DISPLAYED = 4;
 	private static final int LEADING_PAGE_RANGE = 3;
 	private static final int TRAILING_PAGE_RANGE = 3;
 	private static final int NUM_PAGES_OUTSIDE_RANGE = 4;
 	private static final int ADJACENT_PAGES = 2;
 	
 	private Integer totalPages;
 	private Integer currentPage;
 	private Integer pageSize;
 	private String url;
 	
 	@Override
 	public int doStartTag() throws JspException {
 		JspWriter out = pageContext.getOut();
 		try {
 			out.write(generatePaginator());
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return SKIP_BODY;
 	}
 	
 	private String generatePaginator() {
 		
 		boolean in_leading_range = false;
 		boolean in_trailing_range = false;
 		List<Integer> pages_outside_leading_range = new ArrayList<Integer>();
 		List<Integer> pages_outside_trailing_range = new ArrayList<Integer>();
 		List<Integer> page_numbers;
 		
 		if (totalPages <= LEADING_PAGE_RANGE_DISPLAYED) {
 			in_leading_range = in_trailing_range = true;
 			page_numbers = Functions.range(1, totalPages + 1);
 		} else if (currentPage <= LEADING_PAGE_RANGE) {
 			in_leading_range = true;
 			page_numbers = Functions.range(1, LEADING_PAGE_RANGE_DISPLAYED + 1);
 			pages_outside_leading_range = Functions.range(0, -NUM_PAGES_OUTSIDE_RANGE, totalPages);
 		} else if (currentPage > totalPages - TRAILING_PAGE_RANGE) {
 			in_trailing_range = true;
 			page_numbers = Functions.range(totalPages - TRAILING_PAGE_RANGE_DISPLAYED + 1, totalPages + 1);
 			pages_outside_trailing_range = Functions.range(0, NUM_PAGES_OUTSIDE_RANGE, 1);
 		} else {
 			page_numbers = Functions.range(currentPage - ADJACENT_PAGES, currentPage + ADJACENT_PAGES + 1);
 			pages_outside_leading_range = Functions.range(0, -NUM_PAGES_OUTSIDE_RANGE, totalPages);
 			pages_outside_trailing_range = Functions.range(0, NUM_PAGES_OUTSIDE_RANGE, 1);
 		}
 		
 		StringBuilder html = new StringBuilder();
 		html.append("<div class='pagination'>");
		String pageUrl = url.contains("?") ? url + "&" : url + "?";
 		if (currentPage > 1) {
 			html.append("<span class='prev'><a href='");
			html.append(pageUrl);
			html.append("p=" + Integer.toString(currentPage-1));
 			html.append("&ps=" + Integer.toString(pageSize));
 			html.append("'>&laquo; Previous</a></span>");
 		} else {
 			html.append("<span class='prev-na'>&laquo; Anterior</span>");
 		}
 		if (!in_leading_range) {
 			for (Integer num : pages_outside_trailing_range) {
 				html.append("<span class='page'><a href='");
				html.append(pageUrl);
				html.append("p=" + Integer.toString(num));
 				html.append("&ps=" + Integer.toString(pageSize));
 				html.append("'>");
 				html.append(Integer.toString(num));
 				html.append("</a></span>");
 			}
 			html.append("...");
 		}
 		if (page_numbers.size() > 0) {
 			for (Integer num : page_numbers) {
 				if (num <= totalPages) {
 					if (currentPage.equals(num)) {
 						html.append("<span class='curr'>");
 						html.append(Integer.toString(num));
 						html.append("</span>");
 					} else {
 						html.append("<span class='page'><a href='");
						html.append(pageUrl);
						html.append("p=" + Integer.toString(num));
 						html.append("&ps=" + Integer.toString(pageSize));
 						html.append("'>");
 						html.append(Integer.toString(num));
 						html.append("</a></span>");				
 					}
 				}
 			}
 		} else {
 			html.append("<span class='curr'>1</span>");
 		}
 		Collections.reverse(pages_outside_leading_range);
 		if (!in_trailing_range) {
 			html.append("...");
 			for (Integer num : pages_outside_leading_range) {
 				html.append("<span class='page'><a href='");
				html.append(pageUrl);
				html.append("p=" + Integer.toString(num));
 				html.append("&ps=" + Integer.toString(pageSize));
 				html.append("'>");
 				html.append(Integer.toString(num));
 				html.append("</a></span>");
 			}			
 		}
 		if (currentPage < totalPages) {
 			html.append("<span class='next'><a href='");
			html.append(pageUrl);
			html.append("p=" + Integer.toString(currentPage+1));
 			html.append("&ps=" + Integer.toString(pageSize));
 			html.append("'>Next &raquo;</a></span>");
 		} else {
 			html.append("<span class='next-na'>Siguiente &raquo;</span>");			
 		}
 		return html.toString();
 	}
 
 	@Override
 	public int doEndTag() throws JspException {
 		return EVAL_PAGE;
 	}
 
 	public void setTotalPages(Integer totalPages) {
 		this.totalPages = totalPages;
 	}
 
 	public void setCurrentPage(Integer currentPage) {
 		this.currentPage = currentPage;
 	}
 
 	public void setUrl(String url) {
 		this.url = url;
 	}
 
 	public void setPageSize(Integer pageSize) {
 		this.pageSize = pageSize;
 	}
 	
 }
