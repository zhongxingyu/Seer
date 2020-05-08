 /*
  * The MIT License
  *
  * Copyright 2013 Oleg Nenashev <nenashev@synopsys.com>, Synopsys Inc.
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package com.synopsys.arc.jenkinsci.plugins.dynamic_search.views;
 
 import hudson.Util;
 import hudson.model.AbstractProject;
 import hudson.model.Descriptor;
 import hudson.model.Item;
 import hudson.model.TopLevelItem;
 import hudson.model.View;
 import hudson.util.DescribableList;
 import hudson.views.ViewJobFilter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.SortedSet;
 import java.util.TreeSet;
 import java.util.regex.Pattern;
 import javax.servlet.ServletException;
 import org.kohsuke.stapler.StaplerRequest;
 
 /**
  * Implements Jobs filter for SearchView.
  * @author Oleg Nenashev <nenashev@synopsys.com>, Synopsys Inc.
  * @see SimpleSearchView
  * @since 0.1
  */
 public class JobsFilter {
         
     /**
      * Jobs filters
      */
     private DescribableList<ViewJobFilter, Descriptor<ViewJobFilter>> jobFilters;
 
     /**
      * Include regex string.
      */
     private String includeRegex;
     
     /**
      * Compiled include pattern from the includeRegex string.
      */
     private transient Pattern includePattern;
 
     /**
      * Filter by enabled/disabled status of jobs.
      * Null for no filter, true for enabled-only, false for disabled-only.
      */
     private Boolean statusFilter;
     
     /**
      * Constructs empty filter.
      */
     public JobsFilter(View owner) {
         this.statusFilter = null;
         this.jobFilters = new DescribableList<ViewJobFilter, Descriptor<ViewJobFilter>>(owner);
         this.includeRegex = null;        
     }
     
     /**
      * Constructs filter from StaplerRequest.
      * This constructor is just a modified copy of ListView's configure method.
      * @param req Stapler Request
      * @param parentView Parent View, which has created filter
      * @throws hudson.model.Descriptor.FormException
      * @throws IOException
      * @throws ServletException 
      */
     public JobsFilter(StaplerRequest req, View parentView) 
             throws Descriptor.FormException, IOException, ServletException {
         if (req.getParameter("useincluderegex") != null) {
             includeRegex = Util.nullify(req.getParameter("_.includeRegex"));
             if (includeRegex == null)
                 includePattern = null;
             else
                 includePattern = Pattern.compile(includeRegex);
         } else {
             includeRegex = null;
             includePattern = null;
         }
   
         if (jobFilters == null) {
             jobFilters = new DescribableList<ViewJobFilter,Descriptor<ViewJobFilter>>(parentView);
         }
         jobFilters.rebuildHetero(req, req.getSubmittedForm(), ViewJobFilter.all(), "jobFilters");
 
         String filter = Util.fixEmpty(req.getParameter("statusFilter"));
         statusFilter = filter != null ? "1".equals(filter) : null;
     }
     
     public List<TopLevelItem> doFilter(List<TopLevelItem> input, View view) {
         SortedSet<String> names;
 
         synchronized (this) {
             names = new TreeSet<String>();
         }
         
        for (Item item : view.getOwnerItemGroup().getItems()) {
            String itemName = item.getName();
            if (includePattern != null && includePattern.matcher(itemName).matches()) {
                names.add(itemName);
            } else { //Default - add all jobs
                names.add(itemName);
             }
         }
  
         Boolean localStatusFilter = this.statusFilter; // capture the value to isolate us from concurrent update
         List<TopLevelItem> items = new ArrayList<TopLevelItem>(names.size());
         for (String n : names) {
             TopLevelItem item = view.getOwnerItemGroup().getItem(n);
             // Add if no status filter or filter matches enabled/disabled status:
             if(item!=null && (localStatusFilter == null 
                     || !(item instanceof AbstractProject)
                     || ((AbstractProject)item).isDisabled() ^ localStatusFilter)) {
                 items.add(item);
             }
         }
 
         // Check the filters
         Iterable<ViewJobFilter> localJobFilters = getJobFilters();
         List<TopLevelItem> allItems = new ArrayList<TopLevelItem>(view.getOwnerItemGroup().getItems());
     	for (ViewJobFilter jobFilter: localJobFilters) {
     		items = jobFilter.filter(items, allItems, view);
         }
         
         return items;
     }
     
     public DescribableList<ViewJobFilter, Descriptor<ViewJobFilter>> getJobFilters() {
     	return jobFilters;
     }
 
     public Pattern getIncludePattern() {
         return includePattern;
     }
 
     public String getIncludeRegex() {
         return includeRegex;
     }
 
     public Boolean getStatusFilter() {
         return statusFilter;
     }
 }
     
