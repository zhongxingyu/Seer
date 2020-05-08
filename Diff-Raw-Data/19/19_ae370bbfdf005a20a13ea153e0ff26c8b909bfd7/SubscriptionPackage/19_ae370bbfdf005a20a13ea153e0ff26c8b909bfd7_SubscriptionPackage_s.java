 package com.hts.entity;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.ArrayList;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToMany;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 
 import net.sf.json.JSONObject;
 
 import com.hts.exceptions.AppException;
 
 @Entity
 @Table(name = "SUBSCRIPTIONPACKAGE")
 public class SubscriptionPackage {
 
 	public SubscriptionPackage() {
 		super();
 	}
 
 	public SubscriptionPackage(String subscriptionPackageName) {
 		super();
 		this.subscriptionPackageName = subscriptionPackageName;
 	}
 
 	private Integer id;
 	private String subscriptionPackageName;
 	private String description;
 	private String cost;
 
 	@Id
 	@GeneratedValue
 	@Column(name = "SUBSCRIPTIONPACKAGE_ID", nullable = false)
 	public Integer getId() {
 		return id;
 	}
 
 	public void setId(Integer id) {
 		this.id = id;
 	}
 
 	@Column(name = "SUBSCRIPTIONPACKAGE_NAME", nullable = false)
 	public String getSubscriptionPackageName() {
 		return this.subscriptionPackageName;
 	}
 
 	public void setSubscriptionPackageName(String subscriptionPackageName) {
 		this.subscriptionPackageName = subscriptionPackageName;
 	}
 
 	@Column(name = "DESCRIPTION")
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	private List<Channel> channels;
 
 	@ManyToMany(cascade = CascadeType.ALL)
 	@JoinTable(name = "SUBSCRIPTION_CHANNEL", joinColumns = { @JoinColumn(name = "SUBSCRIPTIONPACKAGE_ID") }, inverseJoinColumns = { @JoinColumn(name = "CHANNEL_ID") })
 	public List<Channel> getChannels() {
 		return channels;
 	}
 
 	public void setChannels(List<Channel> channels) {
 		this.channels = channels;
 	}
 
 	@Column(name = "COST")
 	public String getCost() {
 		return cost;
 	}
 
 	public void setCost(String cost) {
 		this.cost = cost;
 	}
 
 	@Override
 	public String toString() {
 		return "SubscriptionPackage [id=" + id + ", subscriptionPackageName="
 				+ subscriptionPackageName + ", description=" + description
 				+ ", Cost=" + cost + ", channels=" + channels + "]";
 	}
 
 	@Transient
 	public String getJson() throws AppException {
 		Map<String, String> map = new HashMap<String, String>();
 		map.put("num", id.toString());
 		map.put("subpackagename", subscriptionPackageName);
 		map.put("description", description);
 		map.put("cost", cost);
 		StringBuilder sb = new StringBuilder();
 
 		List<Channel> channel = getChannels();
 		try {
 			for (Channel i : channel) {
 				sb.append(i.getChannelName() + "<br>");
 			}
 		} catch (Exception e) {
 			//System.out.println(subscriptionPackageName);
 			e.printStackTrace();
 		}
 		map.put("channels", sb.toString());
 		JSONObject json = new JSONObject();
 		json.accumulateAll((Map<String, String>) map);
 
 		return json.toString();
 		/*
 		 * 
 		 * {name: "description", width: 175, sortable: false, align: "center",
 		 * editable: true, edittype: "textarea", editoptions:
 		 * {rows:"2",cols:"25"}, title:false}, {name: "cost",width: 75, align:
 		 * "center", editable: true, title: false}, {name: "channels"
 		 */
 	}
 }
