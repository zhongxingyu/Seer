/*
 * Copyright (c) 2010 Dmitry Astapov <dastapov@gmail.com>
 * Distributed under BSD License
 */
 package ua.astapov.jira.plugins.hierarchicalid.config;
 
 import java.util.Iterator;
 
 import com.atlassian.jira.issue.customfields.option.Option;
 import com.atlassian.jira.issue.customfields.option.Options;
 
 public class HierarchicalIDOption {
 
 	/** The id column name. */
 	private String linkTypeName;
 
 	public String getLinkTypeName() {
 		return linkTypeName;
 	}
 
 	public void setLinkTypeName(String linkTypeName) {
 		this.linkTypeName = linkTypeName;
 	}
 
 	/**
 	 * Instantiates a new custom field options.
 	 */
 	public HierarchicalIDOption() {
 		super();
 	}
 
 	/**
 	 * Instantiates a new custom field options.
 	 * 
 	 * @param options
 	 *          the options
 	 */
 	public HierarchicalIDOption(Options options) {
 		for (Iterator<?> iterator = options.iterator(); iterator.hasNext();) {
 			Option option = (Option) iterator.next();
 			String value = option.getValue();
 			
 			setLinkTypeName(value);
 		}
 	}
 }
