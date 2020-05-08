 ﻿package cluster;
 
 import java.io.*;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.htmlparser.util.*;
 
 import com.mysql.jdbc.Constants;
 
 import similarity_judge.Similarity_Judgement;
 
 
 //import crawl.Pages_analysis;
 import crawl.Pages_analysis;
 import crawl.Search_engine_process;
 import crawl.Search_word_process;
 import datapackage.Link_queue;
 import datapackage.Result_Link_Struct;
 
 public class Cluster {
 	/*配置信息*/
 	static final String DBINFO = "localhost:3306/searchdb";
 	static final String DBUSERNAME = "search";
 	static final String DBPASSWD = "search";
 	static final char SEARCH_ENGINE_TYPE = 'B';
 	static final int SIMILARITY_JUDGE_ALGO = 2;
 	
 	ArrayList<Clusteredresult_Queue> list = new ArrayList<Clusteredresult_Queue>();
 	static ArrayList<String> worklist = new ArrayList<String>();
 	
 	public ArrayList<Clusteredresult_Queue> getlist()
 	{
 		return list;
 	}
 	/*获取工作项目的索引*/
 	public static int getwork(String keyword,int showpage)
 	{
 		int worksize = worklist.size(),nowwork = worklist.indexOf(keyword);
 		while(nowwork >= 0)
 		{
 			if(worklist.get(nowwork + 1).equals(String.valueOf(showpage)))//worklist按照关键词、页码一个个排列
 			{
 				break;
 			}
 			else {
 				nowwork = worklist.indexOf(worklist.subList(nowwork + 1, worksize));//继续向后寻找
 			}
 		}
 		return nowwork;//返回工作列表中找到的项目的索引，没找到则为-1
 	}
 	public static void insertwork(String keyword,int showpage)
 	{
 		worklist.add(keyword);
 		worklist.add(String.valueOf(showpage));
 	}
 	public static void removework(int index)
 	{
 		if(index >= 0 && index < worklist.size() - 1)
 		{
 			worklist.remove(index + 1);//删除页码
 			worklist.remove(index);//删除关键词
 		}
 	}
 	/*判断数据是否存在的同时取数据*/
 	public int getdb(String keyword,int showpage) throws SQLException, ClassNotFoundException
 	{
 		Class.forName("com.mysql.jdbc.Driver");	
 		Connection conn = DriverManager.getConnection("jdbc:mysql://" + DBINFO, DBUSERNAME, DBPASSWD);
 		Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);//可滚动的
 		String sql = "Select ID from KeywordTable where keyword = '" + keyword +"'";
 		ResultSet rs = stmt.executeQuery(sql);
 		if(!rs.next())
 		{
 			return 1;//没有找到关键字。需要抓取
 		}
 		sql = "Select * from ResultTable where showpage = " + 
 				showpage +" and keywordid in (Select ID from KeywordTable where keyword = '" + keyword +"')";
 		rs = stmt.executeQuery(sql);
 		int n = 0;
 		Clusteredresult_Queue queue = new Clusteredresult_Queue();
 		if(rs.next())
 		{
 	        try {
 	            rs.last();
	            if(rs.getRow()!=20)
 	            	return 3;//数据库没存全
 	            rs.first();
 	        } catch (Exception e) {
 	            // TODO: handle exception
 	            e.printStackTrace();
 	        }
 			do{
 				String linktitle = rs.getString("linktitle");
 				String linkurl = rs.getString("linkurl");
 				String linkabstract = rs.getString("linkabstract");
 				int id = rs.getInt("formresultnum");
 				int resultnum = Integer.parseInt(rs.getString("resultnum"));
 				if(resultnum != n)
 				{
 					list.add(queue);
 					queue = new Clusteredresult_Queue();
 					n++;
 				}
 				queue.insert(linkurl, linktitle, linkabstract, id);
 			}while(rs.next());
 			if(queue.gethead() != null)//说明最后一个链表有内容
 			{
 				list.add(queue);
 			}
 			return 2;//找到并从注册表中提取，不需要抓取
 		}else
 			return 3;//有关键字但没有这页，需要抓取
 	}
 	public boolean process_simple(String keyword,int showpage) throws SQLException, ClassNotFoundException
 	{//第一次搜索，由于需要抓取内容速度很慢，于是采用比较标题和摘要的方式处理，而后台自动处理抓取部分
 		//System.out.println("Start Searching...");
 		int flag = getdb(keyword, showpage);
 		if(flag == 2)
 			return true;//数据库中有数据，正常返回
 		//ArrayList<String> strlist = new ArrayList<String>();
 		Clusteredresult_Queue queue;
 		
 		for(int i = (showpage-1) * 2 + 1;i <= showpage * 2;i++)
 		{
 			//先调用Search_word_process类处理输入
 			Search_word_process searchword = null;
 			try {
 				searchword = new Search_word_process(SEARCH_ENGINE_TYPE,keyword,i);
 				searchword.handle_search_word_url();
 			} catch (Exception e) {
 				// TODO 自动生成的 catch 块
 				//////////应要求重新输入合法的数据
 				//////////尚未完成
 				e.printStackTrace();
 				//若是非法基础搜索引擎代码 则程序已自动更正为B 并自动执行
 				//但仍会建议再确认一下基础搜索引擎代码 进行更正并重新执行
 				
 			}
 		
 			//调用Search_engine_process类将结果页面中的有效信息抓取出来
 			Search_engine_process search_engine_process = new Search_engine_process();
 			try {
 				search_engine_process.extractLinks(searchword.getsearch_url(), searchword.getsearch_mode(), searchword.getsearch_page());
 			} catch (Exception e) {
 				// TODO 自动生成的 catch 块
 				e.printStackTrace();
 			}
 			
 			//建立一个全局的result_links链接信息块链表接收抓取出的各个链接的信息
 			//////////是否这样处理有待商榷
 			Link_queue result_links = search_engine_process.getresult_links();
 			
 			int Length = result_links.num_of_links();
 			for(int j = 0;j < Length;j++)
 			{
 				Result_Link_Struct res = result_links.get_link(j);
 				String title = "",url = "",abs = "",text = null;
 				try{
 				title = res.getLink_title().toString();
 				url = res.getLink_url().toString();
 				abs = res.getLink_abstract().toString();
 				text = res.getLink_text().toString();
 				}catch(Exception e)
 				{
 					if(text == null)
 						text = "";
 				}
 				//int size = strlist.size();
 				int size = list.size();
 				int k;
 				for(k = 0;k < size;k++)
 				{
 					//if(Similarity_Judgement.similarity_judge(text,strlist.get(k),1))
 					if(Similarity_Judgement.title_judge(abs,list.get(k).gethead().getabs().toString()))
 						break;
 				}
 				if(k == size)//没有近似的
 				{
 					//strlist.add(text);
 					queue =  new Clusteredresult_Queue();
 					queue.insert(url, title, abs, j + (i-1)*10);
 					list.add(queue);
 				}else {//已有近似的
 					list.get(k).insert(url, title, abs, j + (i-1)*10);
 				}
 			}
 		}
 		return false;//还需要后台运行
 	}
 	public boolean process(String keyword,int showpage) throws SQLException, ClassNotFoundException, ParserException, IOException, InterruptedException
 	{
 		System.out.println("后台运行中...");
 		int flag = getdb(keyword, showpage);
 		if(flag == 2)
 			return true;//数据库中有数据，正常返回
 		
 		/*开始判断队列中是否有相同的任务*/
 		int workindex = getwork(keyword, showpage);
 		if(workindex >= 0)//存在相同任务则反复等待数据库写入完成，这里本应该像传统的多线程操作一样建立临界区访问互斥机制，但貌似不会冲突，尚在测试中
 		{
 			while(getdb(keyword, showpage) != 2)//一直等待另一个线程完成抓取，存数据库操作
 			{
 				Thread.sleep(3000);//等待三秒
 			}
 			return false;//相当于进行了抓取任务，所以需要输出
 		}
 		/*没有相同任务，继续进行*/
 		insertwork(keyword, showpage);
 		
 		MyThread mt1 = new MyThread();
 		MyThread mt2 = new MyThread();
 		mt1.keyword = keyword;
 		mt1.i = showpage * 2 - 1;
 		mt2.keyword = keyword;
 		mt2.i = showpage * 2;
 		
 		mt1.start();
 		mt2.start();
 		
 		boolean is_all_thread_finished = false;
 		while (!is_all_thread_finished) {
 			//若没有全部完成 则主线程被阻塞在while循环中
 			is_all_thread_finished = true;
 			is_all_thread_finished = is_all_thread_finished && (!mt1.isAlive()) && (!mt2.isAlive());
 		}
 		
 		System.out.println("开始写数据库...");
 		int size = list.size();
 		Class.forName("com.mysql.jdbc.Driver");	
 		Connection conn = DriverManager.getConnection("jdbc:mysql://" + DBINFO, DBUSERNAME, DBPASSWD);
 		Statement stmt = conn.createStatement();
 
 		String sql;
 		if(flag == 1)//需要在表中添加关键字再添加项
 		{
 			sql = "Insert into KeywordTable(keyword) values('" + keyword + "')";
 			stmt.executeUpdate(sql);
 		}
 		sql = "Select ID from KeywordTable where keyword = '" + keyword +"'";
 		ResultSet rs = stmt.executeQuery(sql);
 		rs.next();
 		int keywordid = Integer.parseInt(rs.getString("ID"));
 		for(int k = 0;k < size;k++)
 		{
 			queue = list.get(k);
 			Clusteredresult_Node p = queue.head;
 			while(p != null)
 			{
 			sql = "Insert into ResultTable(keywordid,linktitle,linkurl,linkabstract,showpage,formresultnum,resultnum) values(" + keywordid +
 					",'" + p.gettitle() +"','" + p.geturl() +"','" + p.getabs() +"'," + showpage + "," + p.getid() + "," + k + ")";
 			stmt.executeUpdate(sql);
 			p = p.next;
 			}
 		}
 		
 		workindex = getwork(keyword, showpage);//因为index可能发生变化必须重新获取，这里其实应该对worklist进行上锁。
 		removework(workindex);//删除任务
 		System.out.println("结束...");
 		return false;//需要输出
 	}
 	
 	ArrayList<String> strlist = new ArrayList<String>();
 	Clusteredresult_Queue queue;
 	
 	
 	class MyThread extends Thread {
 	    public void run() {
 			//先调用Search_word_process类处理输入
 			Search_word_process searchword = null;
 			System.out.println("开始抓取搜索引擎结果信息...");
 			try {
 				searchword = new Search_word_process(SEARCH_ENGINE_TYPE,keyword,i);
 				searchword.handle_search_word_url();
 			} catch (Exception e) {
 				// TODO 自动生成的 catch 块
 				//////////应要求重新输入合法的数据
 				//////////尚未完成
 				e.printStackTrace();
 				//若是非法基础搜索引擎代码 则程序已自动更正为B 并自动执行
 				//但仍会建议再确认一下基础搜索引擎代码 进行更正并重新执行
 				
 			}
 		
 			//调用Search_engine_process类将结果页面中的有效信息抓取出来
 			Search_engine_process search_engine_process = new Search_engine_process();
 			try {
 				search_engine_process.extractLinks(searchword.getsearch_url(), searchword.getsearch_mode(), searchword.getsearch_page());
 			} catch (Exception e) {
 				// TODO 自动生成的 catch 块
 				e.printStackTrace();
 			}
 			
 			//建立一个全局的result_links链接信息块链表接收抓取出的各个链接的信息
 			//////////是否这样处理有待商榷
 			Link_queue result_links = search_engine_process.getresult_links();
 			
 			//调用Pages_analysis类对各个链接进行正文提取
 			System.out.println("开始进行各个链接的正文提取...");
 			Pages_analysis pages_analysis = new Pages_analysis();
 			pages_analysis.analyze_pages_use_thread(result_links);
 			
 			//测试用抓取正文的线程管理代码 其顺序执行 无多线程并发
 //			pages_analysis.analyze_pages(result_links);
 			
 			//输出result_links链接信息块链表
 			//result_links.output_all_links();
 			
 			System.out.println("开始聚类...");
 			int Length = result_links.num_of_links();
 			//BufferedWriter output1 = new BufferedWriter(new FileWriter("a.txt"));
 			for(int j = 0;j < Length;j++)
 			{
 				Result_Link_Struct res = result_links.get_link(j);
 				String title = "",url = "",abs = "",text = null;
 				try{
 				title = res.getLink_title().toString();
 				url = res.getLink_url().toString();
 				abs = res.getLink_abstract().toString();
 				text = res.getLink_text().toString();
 				}catch(Exception e)
 				{
 					if(text == null)
 						text = "";
 				}
 				int size = strlist.size();
 				int k;
 				for(k = 0;k < size;k++)
 				{
 
 					//output1.write("text1: "+ text + "\n");
 					//output1.write("text2: " + strlist.get(k) + "\n");
 					try {
 						if(Similarity_Judgement.similarity_judge(text,strlist.get(k),SIMILARITY_JUDGE_ALGO))
 						{
 							//output1.write("The Same!\n");
 							break;
 						}
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 				if(k == size)//没有近似的
 				{
 					strlist.add(text);
 					queue =  new Clusteredresult_Queue();
 					queue.insert(url, title, abs ,j + (i-1)*10);//这是和process_simple中对应，便于异步刷新
 					list.add(queue);
 				}else {//已有近似的
 					list.get(k).insert(url, title, abs ,j + (i-1)*10);
 				}
 			}
 			//output1.close();
 	    }
 	    String keyword = new String();
 	    public int i = 0;
 	}
 	
 	
 	
 	
 	
 	
 	
 	/*原使用Googleapi处理函数*/
 	/*
 	public void putinlist(String keyword) throws IOException//测试用的谷歌api
 	{
 	 	String a; 
 		a = clustered.Getpage.getPage(keyword);
 	 	a = a.substring(a.indexOf("\"items\": ") + 1);
 	 	
 	 	File file = new File("C://temp.txt");
 	 	String data;
 		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));  
 		StringBuilder b = new StringBuilder();
 		while((data = br.readLine())!=null)  
 		{  
 			 b.append(data);
 		}
 		a = b.substring(b.indexOf("\"items\": ") + 1);
 		
 		for(int i = 0;i < 5;i++)
 		{
 			Clusteredresult_Queue queue = new Clusteredresult_Queue();
 			for(int j = 0;j < 2;j++)
 			{
 				String ur,ti,ab;
 				a = a.substring(a.indexOf("\"title\": ") + 1);
 				ti = a.split("\"",4)[2];
 				a = a.substring(a.indexOf("\"link\": ") + 1);
 				ur = a.split("\"",4)[2];
 				a = a.substring(a.indexOf("\"snippet\": ") + 1);
 				ab = a.split("\"",4)[2];
 				queue.insert(ur, ti, ab);
 			}
 			list.add(queue);
 		}
 	}*/
 	public void main(String[] args) throws IOException, SQLException, ClassNotFoundException, ParserException, InterruptedException {
 		process("a", 1);
 	}
 }
