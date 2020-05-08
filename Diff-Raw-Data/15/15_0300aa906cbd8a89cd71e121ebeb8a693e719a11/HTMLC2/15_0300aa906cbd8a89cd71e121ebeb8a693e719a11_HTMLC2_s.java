 package supersql.codegenerator.Mobile_HTML5;
 
 import supersql.codegenerator.*;
 import supersql.common.GlobalEnv;
 import supersql.common.Log;
 import supersql.extendclass.ExtList;
 
 
 public class HTMLC2 extends Connector {
 
     Manager manager;
 
     HTMLEnv html_env;
     HTMLEnv html_env2;
     
     static boolean tableFlg = false;		//20130314  table
     static boolean table0Flg = false;		//20130325  table0
     static boolean divFlg = false;			//20130326  div
 
     //���󥹥ȥ饯��
     public HTMLC2(Manager manager, HTMLEnv henv, HTMLEnv henv2) {
         this.manager = manager;
         this.html_env = henv;
         this.html_env2 = henv2;
     }
 
     //C2��work�᥽�å�
     public void work(ExtList data_info) {
         int panelFlg = 0;	//20130503  Panel
     	
         Log.out("------- C2 -------");
         Log.out("tfes.contain_itemnum=" + tfes.contain_itemnum());
         Log.out("tfessize=" + tfes.size());
         Log.out("countconnetitem=" + countconnectitem());
         
         this.setDataList(data_info);
 
         if(decos.containsKey("form")){
         	html_env2.code.append("<form"+HTMLEnv.getFormNumber()+"start />");
         	if(decos.getStr("form").toLowerCase().equals("search")){
         		HTMLEnv.setSearch(true);
         	}
         }	
         
         if(decos.containsKey("insert")){
         	HTMLEnv.setIDU("insert");
         }
         if(decos.containsKey("update")){
         	HTMLEnv.setIDU("update");
         }
         if(decos.containsKey("delete")){
         	HTMLEnv.setIDU("delete");
         }
 
         //tk start////////////////////////////////////////////////////////////////
         html_env.append_css_def_td(HTMLEnv.getClassID(this), this.decos);
         
     	
         //20130325  table0
         if(decos.containsKey("table0"))	table0Flg = true;
         else							table0Flg = false;
         //20130314  table
         //if(decos.containsKey("table") || !decos.containsKey("div") || table0Flg || HTMLC1.tableFlg || HTMLG1.tableFlg || HTMLG2.tableFlg){
         if(decos.containsKey("table") || table0Flg || HTMLC1.tableFlg || HTMLG1.tableFlg || HTMLG2.tableFlg){
 //        if(decos.containsKey("table") || table0Flg){
         //if(decos.containsKey("table") || table0Flg || HTMLC1.tableFlg || HTMLC2.tableFlg || HTMLG1.tableFlg || HTMLG2.tableFlg){
     	//if(decos.containsKey("table") || HTMLC1.tableFlg || HTMLG1.tableFlg){
 //    		Log.info("C2!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
 //    		Log.info("C2 tableFlg = true !!");
     		tableFlg = true;
     	}//else	tableFlg = false;
         
         //20130326  div
         if(decos.containsKey("div")){
         //if(decos.containsKey("div") && !HTMLC1.tableFlg && !HTMLG1.tableFlg && !HTMLG2.tableFlg){
         //if((decos.containsKey("div") && !HTMLC1.tableFlg && !HTMLC2.tableFlg && !HTMLG1.tableFlg && !HTMLG2.tableFlg)
         //		|| HTMLC1.divFlg || HTMLC2.divFlg || HTMLG1.divFlg || HTMLG2.divFlg){
     	//if(decos.containsKey("div") || HTMLC1.divFlg || HTMLC2.divFlg || HTMLG1.divFlg || HTMLG2.divFlg){
     	//if(decos.containsKey("div") || HTMLC1.divFlg || HTMLG1.divFlg || HTMLG2.divFlg){
     		divFlg = true;
     		tableFlg = false;
     	}//else divFlg = false;
         
         if(!GlobalEnv.isOpt()){
         	//20130503  Panel
     	    panelFlg = HTMLC1.panelProcess1(decos, html_env);
         	
         	//20130330 tab
         	//tab1
         	if(decos.containsKey("tab1")){
             	html_env.code.append("<div data-role=\"content\"> <div id=\"tabs\">\n<ul>\n");
             	html_env.code.append("	<li><a href=\"#tabs-"+HTMLEnv.tabCount+"\">");
             	if(!decos.getStr("tab1").equals(""))	html_env.code.append(decos.getStr("tab1"));
             	else          							html_env.code.append("tab1");
             	html_env.code.append("</a></li>\n");
             	html_env.code.append("</ul>\n<div id=\"tabs-"+HTMLEnv.tabCount+"\">\n");
 //            	HTMLEnv.tabFlg = true;
             }
         	//tab2〜tab15
 //        	else if(HTMLEnv.tabFlg){
         	else{
         		int i=2;
         		while(i<=HTMLEnv.maxTab){		//HTMLEnv.maxTab=15
         			//Log.info("i="+i+" !!");
         			if(decos.containsKey("tab"+i) || (i==2 && decos.containsKey("tab"))){
     	        		//replace: </ul>の前に<li>〜</li>を付加
     	        		String a = "</ul>";
     	        		String b = "	<li><a href=\"#tabs-"+HTMLEnv.tabCount+"\">";
     	        		if(decos.containsKey("tab"+i))
 	    	        		if(!decos.getStr("tab"+i).equals(""))	b += decos.getStr("tab"+i);
 	    	            	else				            		b += "tab"+i;
     	        		else
     	        			if(!decos.getStr("tab").equals(""))		b += decos.getStr("tab");
 	    	            	else				            		b += "tab";
     	            	b += "</a></li>\n";
     	            	HTMLManager.replaceCode(html_env, a, b+a);
     	            	
     	            	//replace: 最後の</div></div></div>カット
     	        		HTMLManager.replaceCode(html_env, "</div></div></div>", "");
     	        		
     	        		//replace: 不要な「<div class=〜」をカット
     	        		HTMLManager.replaceCode(html_env, "<div class=\""+HTMLEnv.getClassID(this)+" \">", "");
     	        		
     	            	html_env.code.append("<div id=\"tabs-"+HTMLEnv.tabCount+"\">\n");
     	            	break;
     	        	}
         			i++;
 //        			if(i>HTMLEnv.maxTab)	HTMLEnv.tabFlg =false;
         		}
         	}
         	
         	//20130312 collapsible
         	if(decos.containsKey("collapse")){
             	html_env.code.append("<DIv data-role=\"collapsible\" data-content-theme=\"c\" style=\"padding: 0px 12px;\">\n");
             	
             	//header
             	if(!decos.getStr("collapse").equals(""))
             		html_env.code.append("	<h1>"+decos.getStr("collapse")+"</h1>\n");
             	else
             		html_env.code.append("<h1>Contents</h1>\n");
             }
         	
 //        	//changed by goto 20130309  border=0
         	//20130309
         	//20130314  table
         	if(tableFlg){
         		//added 20130314  table width="95%" align="center"
         		html_env.code.append("<TABLE width=\"100%\" cellSpacing=\"0\" cellPadding=\"0\" border=\"");
         		//html_env.code.append("<TABLE width=\"100%\" align=\"center\" cellSpacing=\"0\" cellPadding=\"0\" border=\"");
         		//html_env.code.append("<TABLE width=\"95%\" align=\"center\" cellSpacing=\"0\" cellPadding=\"0\" border=\"");
 //	        	html_env.code.append("<TABLE cellSpacing=\"0\" cellPadding=\"0\" border=\"");
         		//html_env.code.append(((!table0Flg)? html_env.tableborder : "0") + "\"");
         		if(table0Flg || HTMLC1.table0Flg || HTMLG1.table0Flg || HTMLG2.table0Flg)
         			html_env.code.append("0" + "\"");	//20130325 table0
 	        	else	html_env.code.append(html_env.tableborder + "\"");
 //	        	html_env.code.append(html_env.tableborder+ "\" ");
 	        	html_env.code.append(html_env.getOutlineMode());
 	        	if(html_env.written_classid.contains(HTMLEnv.getClassID(this))){
 	        		html_env.code.append(" class=\"");
 	        		html_env.code.append(HTMLEnv.getClassID(this));
 	        	}
 	
 	        	if(decos.containsKey("class")){
 	        		if(!html_env.written_classid.contains(HTMLEnv.getClassID(this))){
 	        			html_env.code.append(" class=\"");
 	        		}else{
 	        			html_env.code.append(" ");
 	        		}
 	        		html_env.code.append(decos.getStr("class") + "\" ");   	
 	        	}else if(html_env.written_classid.contains(HTMLEnv.getClassID(this))){
 	        		html_env.code.append("\" ");
 	        	}
 	        	html_env.code.append(">");
         	}
         }
         if(GlobalEnv.isOpt()){
             html_env2.code.append("<tfe type=\"connect\" dimension=\"2\"");
         	if (decos.containsKey("tablealign") )
         		html_env2.code.append(" align=\"" + decos.getStr("tablealign") +"\"");
 //        	else	//added 20130314
 //        		html_env2.code.append(" align=\"center\"");
 //        	if (decos.containsKey("tablevalign") )
         		html_env2.code.append(" valign=\"" + decos.getStr("tablevalign") +"\"");
 //        	else	//added 20130314
 //        		html_env2.code.append(" valign=\"middle\"");
         	if (decos.containsKey("height") )
         		html_env2.code.append(" height=\"" + decos.getStr("height") +"\"");
         	if(decos.containsKey("tabletype")){
         		html_env2.code.append(" tabletype=\"" + decos.getStr("tabletype") + "\"");
         		if(decos.containsKey("cellspacing")){
         			html_env2.code.append(" cellspacing=\"" + decos.getStr("cellspacing") + "\"");
         		}
         		if(decos.containsKey("cellpadding")){
         			html_env2.code.append(" cellpadding=\"" + decos.getStr("cellpadding") + "\"");
         		}
         		if(decos.containsKey("border")){
         			html_env2.code.append(" border=\"" + decos.getStr("border").replace("\"", "") + "\"");
         		}
 
 	        	if(decos.containsKey("tableborder")){
         			html_env2.code.append(" tableborder=\"" + decos.getStr("tableborder").replace("\"", "") + "\"");
         		}
         	}else{
         		if(decos.containsKey("border")){
         			html_env2.code.append(" border=\"" + decos.getStr("border").replace("\"", "") + "\"");
         		}else{
             		html_env2.code.append(" border=\"" + html_env.tableborder.replace("\"", "") +"\"");
         		}
 	        	if(decos.containsKey("tableborder")){
         			html_env2.code.append(" tableborder=\"" + decos.getStr("tableborder").replace("\"", "") + "\"");
         		}
         	}
         	if(html_env.written_classid.contains(HTMLEnv.getClassID(this))){
         		html_env2.code.append(" class=\"");
         		html_env2.code.append(HTMLEnv.getClassID(this));
         	}
 
         	if(decos.containsKey("class")){
         		if(!html_env.written_classid.contains(HTMLEnv.getClassID(this))){
         			html_env2.code.append(" class=\"");
         		}else{
         			html_env2.code.append(" ");
         		}
         		html_env2.code.append(decos.getStr("class"));        	
         	}else if(html_env.written_classid.contains(HTMLEnv.getClassID(this))){
         		html_env2.code.append("\" ");
         	}
         	
 
 	        if(decos.containsKey("form")){
 	        	html_env2.code.append(" form=\""+ HTMLEnv.getFormNumber() +"\" ");
 	        }	        
 	        
         	html_env2.code.append(">");
         }
         /*
         if(decos.containsKey("outborder"))
         	html_env.code.append(" noborder ");
         	*/
        
         
         //html_env2.code.append(">");
         
         //System.out.println(html_env.code);
                 
         //tk end//////////////////////////////////////////////////////////////////
         
 //        Log.out("<TABLE class=\""+HTMLEnv.getClassID(this) + "\">");
 
         int i = 0;
         
     	if(decos.containsKey("form")){
            	html_env.code.append(HTMLFunction.createForm(decos));
            	HTMLEnv.setFormItemFlg(true,null);
         }
     	
         while (this.hasMoreItems()) {
             //Log.info("C2-1:	"+tableFlg+"	"+decos.containsKey("table")+"	"+decos.containsKey("table0"));
             if(decos.containsKey("table0") || HTMLC1.table0Flg || HTMLG1.table0Flg || HTMLG2.table0Flg)	table0Flg = true;
             //else	table0Flg=false;
             if(decos.containsKey("table") || HTMLC1.tableFlg || HTMLG1.tableFlg || HTMLG2.tableFlg || table0Flg)	tableFlg=true;
 //            if(decos.containsKey("table") || HTMLC1.tableFlg || HTMLC2.tableFlg || HTMLG1.tableFlg || HTMLG2.tableFlg || table0Flg)	tableFlg=true;
             //if(decos.containsKey("table") || table0Flg)	tableFlg=true;
            Log.i("C2:	decos = "+decos+"		"+HTMLC1.tableFlg+" "+HTMLC2.tableFlg+"	tableFlg = "+tableFlg+"	divFlg = "+decos.containsKey("div"));
             if(decos.containsKey("div")){
         	//if(decos.containsKey("div") && !HTMLC1.tableFlg && !HTMLG1.tableFlg && !HTMLG2.tableFlg){
             //if((decos.containsKey("div") && !HTMLC1.tableFlg && !HTMLC2.tableFlg && !HTMLG1.tableFlg && !HTMLG2.tableFlg)
             //		|| HTMLC1.divFlg || HTMLC2.divFlg || HTMLG1.divFlg || HTMLG2.divFlg){
 //            if(decos.containsKey("div") || HTMLC1.divFlg || HTMLC2.divFlg || HTMLG1.divFlg || HTMLG2.divFlg){
 //            if(decos.containsKey("div") || HTMLC1.divFlg || HTMLG1.divFlg || HTMLG2.divFlg){
         		divFlg = true;
         		tableFlg = false;
         	}//else divFlg = false;
             
             TFE tfe = (TFE) tfes.get(i);
             
             //20130312 collapsible
 	      	if(decos.containsKey("collapse"))
 	          	html_env.code.append("<p>\n");
 	      	else if(!tableFlg) 
 	            //20130309
 	            //x html_env.code.append("	<DIV Class=\"ui-grid-a\">	<div class=\"ui-block-a\">");		
 	            html_env.code.append("<div class=\""+HTMLEnv.getClassID(tfe)+" \">\n");
 	        
 	      	//20130314  table
 	      	String classid = null ;
 	      	if(tableFlg){
 	      		//x html_env.code.append("	<Table><tr><td>\n");
 	      		//added 20130314  table align="center" valign="middle"
 	      		html_env.code.append("<TR><TD valign=\"middle\" class=\""
 	      		//html_env.code.append("<TR><TD align=\"center\" valign=\"middle\" class=\""
 //        	   	html_env.code.append("<TR><TD class=\""
                    + HTMLEnv.getClassID(tfe) + " nest\">\n");
 	      		classid = HTMLEnv.getClassID(tfe);
 	      		Log.out("<TR><TD class=\"nest "
                     + HTMLEnv.getClassID(tfe) + " nest\"> decos:" + decos);
 	      	}
         	
         	//Log.info("C2  !!");
             this.worknextItem();
             //Log.info("C2-2:	"+tableFlg+"	"+decos.containsKey("table")+"	"+decos.containsKey("table0"));
            
             if(decos.containsKey("table0") || HTMLC1.table0Flg || HTMLG1.table0Flg || HTMLG2.table0Flg)	table0Flg = true;
             //else	table0Flg=false;
             if(decos.containsKey("table") || HTMLC1.tableFlg || HTMLG1.tableFlg || HTMLG2.tableFlg || table0Flg)	tableFlg=true;
             //if(decos.containsKey("table") || HTMLC1.tableFlg || HTMLC2.tableFlg || HTMLG1.tableFlg || HTMLG2.tableFlg || table0Flg)	tableFlg=true;
 //            if(decos.containsKey("table") || table0Flg)	tableFlg=true;
             if(decos.containsKey("div")){
         	//if(decos.containsKey("div") && !HTMLC1.tableFlg && !HTMLG1.tableFlg && !HTMLG2.tableFlg){
             //if(decos.containsKey("div") && (HTMLC1.divFlg || HTMLC2.divFlg || HTMLG1.divFlg || HTMLG2.divFlg)){
             //if((decos.containsKey("div") && !HTMLC1.tableFlg && !HTMLC2.tableFlg && !HTMLG1.tableFlg && !HTMLG2.tableFlg)
             //		|| HTMLC1.divFlg || HTMLC2.divFlg || HTMLG1.divFlg || HTMLG2.divFlg){
             //if(decos.containsKey("div") || HTMLC1.divFlg || HTMLC2.divFlg || HTMLG1.divFlg || HTMLG2.divFlg){
 //            if(decos.containsKey("div") || HTMLC1.divFlg || HTMLG1.divFlg || HTMLG2.divFlg){
         		divFlg = true;
         		tableFlg = false;
         	}//else divFlg = false;
 
             //20130306
             //20130314  table
 	      	if(tableFlg){
 	      		try{
 	            if (html_env.not_written_classid.contains(classid)){
 	            	html_env.code.delete(html_env.code.indexOf(classid),html_env.code.indexOf(classid)+classid.length()+1);
 	            }
 	      		}catch(Exception e){}
 	            //x html_env.code.append("	</td></tr></Table>");
 	      	}
           
             //20130314  table
 	      	if(tableFlg)
 	      		html_env.code.append("</TD></TR>\n");
 	      	//Log.out("</TD></TR>");
 	      	
             //20130312 collapsible
 	      	if(decos.containsKey("collapse"))
 	          	html_env.code.append("</p>\n");
 	      	else if(!tableFlg)
 	        	html_env.code.append("\n</div>");
             
             html_env.code.append("\n");		//20130309
 
             i++;
 
         }
 
         html_env2.code.append("</tfe>");
         //Log.out("</TABLE>");
         if(decos.containsKey("form")){
         	html_env2.code.append("<form"+ HTMLEnv.getFormNumber() +"end />");
         	html_env.code.append(HTMLEnv.exFormNameCreate());
            	html_env.code.append("</form>");
            	HTMLEnv.setFormItemFlg(false,null);
            	HTMLEnv.incrementFormNumber();
            	if(decos.getStr("form").toLowerCase().equals("search"))
         		HTMLEnv.setSearch(false);
         }
 
       	//20130314  table
       	if(tableFlg){
       		html_env.code.append("</TABLE>\n");		//20130309
      		tableFlg = false;Log.i("tableFlg = false! 2");
       		table0Flg = false;		//20130325 table0
       	}
       	
         //20130312 collapsible
       	if(decos.containsKey("collapse")){
           	html_env.code.append("</DIv>");
         }
       	
     	//20130330 tab
 //    	if(HTMLEnv.tabFlg){
     		int a=1;
 	    	while(a<=HTMLEnv.maxTab){
 	    		//Log.info("a="+a);
 	    		if(decos.containsKey("tab"+a) || (a==1 && decos.containsKey("tab"))){
 		    		html_env.code.append("</div></div></div>\n");
 		    		HTMLEnv.tabCount++;
 		    		break;
 		    	}
 		    	a++;
 	    	}
 //    	}
       	
     	//20130503  Panel
     	HTMLC1.panelProcess2(decos, html_env, panelFlg);
 	    	
       	if(divFlg)	divFlg = false;		//20130326  div
 
         Log.out("TFEId = " + HTMLEnv.getClassID(this));
         //html_env.append_css_def_td(HTMLEnv.getClassID(this), this.decos);
         //↑必要？不要？？ -> 不要
     }
 
     public String getSymbol() {
         return "HTMLC2";
     }
 
 }
