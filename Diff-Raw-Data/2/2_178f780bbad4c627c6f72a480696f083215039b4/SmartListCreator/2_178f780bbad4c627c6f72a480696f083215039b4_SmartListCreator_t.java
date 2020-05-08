 package com.rtm;
 
 import com.mainchip.Java.addMilk.*;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.htmlunit.HtmlUnitDriver;
 
 import java.io.IOException;
 import java.util.*;
 
 public class SmartListCreator {
 
 	public static final String RTM_API_KEY = "fc0879c6de59e019d3de57ca6a8a0324";
 	public static final String RTM_SECRET = "14d7a12394371d57";
 
 	public static void main(String[] args) throws MilkException, IOException {
 
 
 		RTM rtm = new RTM(RTM_API_KEY, RTM_SECRET);
 
 		String frob = rtm.getFrob();
 		String authUrl = rtm.genAuthURL(frob, "write");
 
 		WebDriver driver = new HtmlUnitDriver();
 		driver.get(authUrl);
 		driver.findElement(By.id("username")).sendKeys("aka_lordy");
 		driver.findElement(By.id("password")).sendKeys("frbvxtu");
 		driver.findElement(By.id("remember")).click();
 		driver.findElement(By.id("login-button")).click();
 		try {
 			driver.findElement(By.id("authorize_yes")).click();
 		} catch (Exception e) {
 
 		}
 
 		Token tocken = rtm.getToken(frob);
 		rtm.setToken(tocken.getToken());
 
 		Vector<TaskList> listOfTaskLists = rtm.getListOfTaskLists();
 		List<String> tasklistsNames = new ArrayList<String>();
 
 		Vector<Taskseries> alltasks = new Vector<Taskseries>();
 		Vector<String> filtersList = new Vector<String>();
 		for (TaskList tl : listOfTaskLists) {
 			tasklistsNames.add(tl.getName());
			Vector<TaskList> tasks = rtm.getTaskList(tl.getID(), "status:incomplete", "");
 
 			String filter = tl.getFilter();
 			if (filter != null) {
 				filtersList.add(filter);
 			}
 			if (tasks == null) continue;
 			TaskList list = tasks.get(0);
 			Vector<Taskseries> taskserieses = list.getSeries();
 			if (taskserieses != null && !tl.isSmart()) {
 				alltasks.addAll(taskserieses);
 			}
 		}
 		Set<String> allTags = new HashSet<String>();
 		for (Taskseries task : alltasks) {
 			Vector<String> taskTags = task.getTags();
 			if (taskTags != null)
 				allTags.addAll(taskTags);
 		}
 
 		for (String tag : allTags) {
 			TaskList list = null;
 			String listName = "";
 			if (tag.startsWith("+")) {
 				listName = "P:" + tag.replace("+", "");
 			}
 			if (tag.startsWith("@")) {
 				listName = tag;
 			}
 			if (!filtersList.contains("(tag:" + tag + ")") && !tasklistsNames.contains(listName)) {
 				String listTimeline = rtm.createTimeline();
 				list = rtm.addTaskList(listTimeline, listName, "tag:" + tag);
 			}
 
 			if (list != null) {
 				System.out.println("New SmartList " + list.getName() + " with filter " + list.getFilter() + " created");
 			}
 		}
 
 	}
 }
