 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.lorent.lvmc.controller;
 
 import com.lorent.common.util.ParaUtil;
 import com.lorent.lvmc.bean.OptionDto;
 import com.lorent.lvmc.bean.VoteComboxItemModel;
 import com.lorent.lvmc.bean.VoteDataChildDto;
 import com.lorent.lvmc.bean.VoteDataDto;
 import com.lorent.lvmc.bean.VoteItemResultBean;
 import com.lorent.lvmc.bean.VoteItemResultOptionBean;
 import com.lorent.lvmc.dto.LoginInfo;
 import com.lorent.lvmc.ui.MultiSelectPanel;
 import com.lorent.lvmc.ui.SelectItemPanel;
 import com.lorent.lvmc.ui.SingleSelectPanel;
 import com.lorent.lvmc.ui.VoteTabItemPane;
 import com.lorent.lvmc.ui.YesorNoPanel;
 import com.lorent.lvmc.ui.voteMainPanel;
 import com.lorent.lvmc.util.Constants;
 import com.lorent.lvmc.util.DataUtil;
 import com.lorent.lvmc.util.StringUtil;
 import com.lorent.lvmc.util.XmlUtilParse;
 import java.awt.GridLayout;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 
 import javax.swing.BoxLayout;
 import javax.swing.JComboBox;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTabbedPane;
 
 import org.apache.log4j.Logger;
 import org.jivesoftware.smack.Connection;
 
 /**
  *
  * @author test
  */
 public class VoteController extends BaseController{
     private static Logger log = Logger.getLogger(VoteController.class);
     public void addNewTheme(ParaUtil paras)throws Exception{
         String xml=paras.getValue("addNewtheme");
         log.debug(xml);
         services.getVoteService().sendVoteData(xml);
     }
     public void updateTheme(ParaUtil paras)throws Exception{
         String xml=paras.getValue("updatetheme");
         services.getVoteService().sendVoteData(xml);
     }
     public void deleteTheme(ParaUtil paras){
     
     }
     public void comiteResult(ParaUtil paras)throws Exception{
         String xml=paras.getValue("comitevote");
         services.getVoteService().sendVoteData(xml);
     }
     public void addNewVoteItem(ParaUtil paras)throws Exception{
         String xml=paras.getValue("addNewItem");
         services.getVoteService().sendVoteData(xml);
     }
     public void updateVoteItem(ParaUtil paras)throws Exception{
         String xml=paras.getValue("updateItem");
         services.getVoteService().sendVoteData(xml);
     }
     public void deleteVoteItem(ParaUtil paras)throws Exception{
         String xml=paras.getValue("deleteItem");
         services.getVoteService().sendVoteData(xml);
     
     }
     public void load_Vote(ParaUtil paras)throws Exception{
         String xml=paras.getValue("load_vote");
         services.getVoteService().sendVoteData(xml);
     }
     
     public void getVoteResult(ParaUtil paras)throws Exception{
         String xml=paras.getValue("voteresult");
         services.getVoteService().sendVoteData(xml);
       
     }
     public void search_Voted_record(ParaUtil paras)throws Exception{
         String themeid=paras.getValue("themeid");
         String xml=getSearchVotedRecord(themeid);
         services.getVoteService().sendVoteData(xml);
     }
     
     public void refreshComboBox(ParaUtil para){
         try {
             mainPanel.setInitFlag(0);
             Iterator iter = voteMap.entrySet().iterator();
             while (iter.hasNext()) {
                 Map.Entry entry = (Map.Entry) iter.next();
                 String themekey = (String) entry.getKey();
                 VoteDataDto val = (VoteDataDto) entry.getValue();
             }
             mainPanel.setInitFlag(1);
         } catch (Exception ex) {
             log.error("refreshComboBox", ex);
         }
 
     }
     
     
     public void errorMsg(ParaUtil para){
        String errorMsg=para.getValue("errorMsg");
        this.showErrorDialog(StringUtil.getErrorString("error.title"), para.getValue("returnMsg")==null?errorMsg:para.getValue("returnMsg").toString());
        return;
     }
     //创建主题成功
     public void create_voteSuccess(XmlUtilParse util){
     	VoteTabItemPane voteTabItemPane=new VoteTabItemPane();
         try {
 //            int count=mainPanel.getjTabbedPane1().getTabCount();
             int count=getVoteMainPanel().getjTabbedPane1().getTabCount();
             mainPanel.getjTabbedPane1().addTab(util.getElementValue("title")!=null?util.getElementValue("title").toString():null, voteTabItemPane);
             mainPanel.getjTabbedPane1().setSelectedIndex(count);
             voteTabItemPane.setId(util.getElementValue("id")!=null?util.getElementValue("id").toString():null);
             mainPanel.setVoteTabItemPane(voteTabItemPane);
             voteTabItemPane.setCreator(util.getElementValue("creator")!=null?util.getElementValue("creator").toString():null);
             voteTabItemPane.getjLabel3().setText(util.getElementValue("title")!=null?util.getElementValue("title").toString():null);
             voteTabItemPane.getjLabel4().setText(util.getElementValue("title_remark")!=null?util.getElementValue("title_remark").toString():null);
             voteTabItemPane.getjLabel5().setText(StringUtil.getUIString("voteStatus.notstart"));
             voteTabItemPane.getSelectItemPanelList().clear();
             voteTabItemPane.buttonStatus(util.getElementValue("creator")!=null?util.getElementValue("creator").toString():null);
             voteTabItemPane.setIsclose("false");
             voteTabItemPane.setIsstar("false");
             voteTabItemPane.setButtonStatus();
             if(!themeCreatorMap.containsKey(util.getElementValue("id"))){
             	themeCreatorMap.put(util.getElementValue("id").toString(),util.getElementValue("creator").toString());
             }
         } catch (Exception ex) {
         	log.error("create_voteSuccess", ex);
         }
     }
     
     private Map<String, String> themeCreatorMap=new HashMap<String, String>();
     //修改主题
     public void update_voteSuccess(XmlUtilParse util) {
         try {
             if (((VoteTabItemPane)mainPanel.getjTabbedPane1().getSelectedComponent()).getId().equals(util.getElementValue("id") != null ? util.getElementValue("id").toString() : null)) {
 //            	((VoteTabItemPane)mainPanel.getjTabbedPane1().getSelectedComponent()).getjLabel3().setText(util.getElementValue("title") != null ? util.getElementValue("title").toString() : null);
             	mainPanel.getjTabbedPane1().setTitleAt(mainPanel.getjTabbedPane1().getSelectedIndex(), util.getElementValue("title") != null ? util.getElementValue("title").toString() :"");
             	((VoteTabItemPane)mainPanel.getjTabbedPane1().getSelectedComponent()).getjLabel4().setText(util.getElementValue("title_remark") != null ? util.getElementValue("title_remark").toString() : null);
             	
                 if ("true".equals(util.getElementValue("is_start")) && "false".equals(util.getElementValue("is_close"))) {
                 	((VoteTabItemPane)mainPanel.getjTabbedPane1().getSelectedComponent()).getjLabel5().setText(StringUtil.getUIString("voteStatus.starting"));
                 }
                 if("true".equals(util.getElementValue("is_close"))){
                 	((VoteTabItemPane)mainPanel.getjTabbedPane1().getSelectedComponent()).getjLabel5().setText(StringUtil.getUIString("voteStatus.end"));
                 }
                 if("false".equals(util.getElementValue("is_start"))){
                 	((VoteTabItemPane)mainPanel.getjTabbedPane1().getSelectedComponent()).getjLabel5().setText(StringUtil.getUIString("voteStatus.notstart"));
                 }
                 ((VoteTabItemPane)mainPanel.getjTabbedPane1().getSelectedComponent()).getjPanel4().removeAll();
                 LoginInfo info=DataUtil.getValue(DataUtil.Key.LoginInfo);
                 for (Iterator<SelectItemPanel> it = ((VoteTabItemPane)mainPanel.getjTabbedPane1().getSelectedComponent()).getSelectItemPanelList().iterator(); it.hasNext();) {
                     SelectItemPanel selectItemPanel = it.next();
                 	//根据当前登录用户是否创建用户进行判断是否显示修改删除按钮
                     if(null!=themeCreatorMap.get(util.getElementValue("id").toString())){
                     	if(null!=themeCreatorMap.get(util.getElementValue("id").toString())&&!info.getUsername().equals(themeCreatorMap.get(util.getElementValue("id").toString()))){
                     		selectItemPanel.getjButton1().setVisible(false);
                     		selectItemPanel.getjButton2().setVisible(false);
                     	}
                     }
                     ((VoteTabItemPane)mainPanel.getjTabbedPane1().getSelectedComponent()).getjPanel4().add(selectItemPanel);
                 }
               //如果表决项面板少于6，则补一些新空面板上去
         		if(((VoteTabItemPane)mainPanel.getjTabbedPane1().getSelectedComponent()).getSelectItemPanelList().size()<6){
         			for(int i=0;i<6-((VoteTabItemPane)mainPanel.getjTabbedPane1().getSelectedComponent()).getSelectItemPanelList().size()+3;i++){
         				((VoteTabItemPane)mainPanel.getjTabbedPane1().getSelectedComponent()).getjPanel4().add(new JPanel());
         			}
         		}
         		//设置主题状态
             	((VoteTabItemPane)mainPanel.getjTabbedPane1().getSelectedComponent()).setIsstar(util.getElementValue("is_start")==null?null:util.getElementValue("is_start").toString());
             	((VoteTabItemPane)mainPanel.getjTabbedPane1().getSelectedComponent()).setIsclose(util.getElementValue("is_close")==null?null:util.getElementValue("is_close").toString());
             	//修改主题成功没有返回creator
             	if(null!=themeCreatorMap.get(util.getElementValue("id").toString())){
             	    ((VoteTabItemPane)mainPanel.getjTabbedPane1().getSelectedComponent()).setCreator(themeCreatorMap.get(util.getElementValue("id").toString()));
                     ((VoteTabItemPane)mainPanel.getjTabbedPane1().getSelectedComponent()).buttonStatus(themeCreatorMap.get(util.getElementValue("id").toString()));
             	}
                 ((VoteTabItemPane)mainPanel.getjTabbedPane1().getSelectedComponent()).setButtonStatus();
                 ((VoteTabItemPane)mainPanel.getjTabbedPane1().getSelectedComponent()).getjPanel4().validate();
                 ((VoteTabItemPane)mainPanel.getjTabbedPane1().getSelectedComponent()).getjPanel4().updateUI();
             }
         } catch (Exception ex) {
         	log.error("update_voteSuccess", ex);
         }
     }
     //增加表决项
     public void insert_selectSuccess(XmlUtilParse util) throws Exception{
         VoteDataChildDto childDto=new VoteDataChildDto();
         String selectItemType=util.getElementValue("type")==null?null:util.getElementValue("type").toString();
         if(null!=selectItemType && !"1".equals(selectItemType)&& !"2".equals(selectItemType))
             selectItemType="0";//如果不是单选，不是多选，就默认是是否选项
         childDto.setItemType(""+selectItemType);
         String title=util.getElementValue("select_title")==null?null:util.getElementValue("select_title").toString();
         childDto.setTitle(title);
         String title_descrition=util.getElementValue("select_remark")==null?null:util.getElementValue("select_remark").toString();
         childDto.setTitledescription(title_descrition);  
         
         Object o=util.getElementValue("id");
         if (o instanceof List) {
             List ids = (ArrayList) (util.getElementValue("id") == null ? null : util.getElementValue("id"));
             List names = (ArrayList) (util.getElementValue("option_name") == null ? null : util.getElementValue("option_name"));
             OptionDto[] answer = new OptionDto[ids.size()-1];
             childDto.setTitleid(ids.get(0).toString());
             for (int i = 0; i < ids.size()-1; i++) {
                 OptionDto optionDto = new OptionDto();
                 optionDto.setId(ids.get(i+1).toString());
                 optionDto.setName(names.get(i).toString());
                 answer[i] = optionDto;
             }
             childDto.getList().add(answer);
         }
         if(o instanceof String){
            String id=util.getElementValue("id")==null?null:util.getElementValue("id").toString();
            childDto.setTitleid(id);
         }
         LoginInfo info=DataUtil.getValue(DataUtil.Key.LoginInfo);
         if(null!=themeCreatorMap.get(util.getElementValue("conference_vote_id").toString())){
         	addSelectItem(childDto,themeCreatorMap.get(util.getElementValue("conference_vote_id").toString()),"false","false");
         }else{
         	addSelectItem(childDto,info.getUsername(),"false","false");
         }
     }
     private Map<String,String> optionMap=getOptionMap();// 存放option id 对应的选项字符 A B C ... 等
     private Map<String,String> getOptionMap(){
          if(null!=DataUtil.getValue(DataUtil.Key.optionMap)){
              return DataUtil.getValue(DataUtil.Key.optionMap);
          }else{
              return new HashMap<String, String>();
          }
     }
     
     public void addSelectItem(VoteDataChildDto childDto,String creator,String isStart,String isClose) throws Exception{
         initConstMap();
         VoteTabItemPane voteTabItemPane=(VoteTabItemPane)mainPanel.getjTabbedPane1().getSelectedComponent();
         SelectItemPanel selectItemPanel = new SelectItemPanel(voteTabItemPane);
         selectItemPanel.getjLabel1().setText("表决项 " + (voteTabItemPane.getSelectItemPanelList().size() + 1));
         selectItemPanel.setItemPanelKey(Constants.ITEMKEY + (voteTabItemPane.getSelectItemPanelList().size() + 1));
         selectItemPanel.getjLabel2().setText(childDto.getTitle());
         selectItemPanel.setId(childDto.getTitleid());
         selectItemPanel.getjLabel4().setText(childDto.getTitledescription());
         LoginInfo info=DataUtil.getValue(DataUtil.Key.LoginInfo);
         //根据当前登录用户是否创建用户进行判断是否显示修改删除按钮
         if(null!=creator&&!info.getUsername().equals(creator)){
             selectItemPanel.getjButton1().setVisible(false);
             selectItemPanel.getjButton2().setVisible(false);
         }
         //判断按钮是否可用
         if("false".equals(isStart)&&"false".equals(isClose)){
             selectItemPanel.getjButton1().setEnabled(true);
             selectItemPanel.getjButton2().setEnabled(true);
         }else{
             selectItemPanel.getjButton1().setEnabled(false);
             selectItemPanel.getjButton2().setEnabled(false);
         }
         for (int i = 0; i < childDto.getList().size(); i++) {
         	selectItemPanel.getjPanel1().setLayout(new BoxLayout(selectItemPanel.getjPanel1(), BoxLayout.Y_AXIS));
             if (childDto.getItemType().equals("" + Constants.SELECTITEMTYPE_MULTI)) {
                 MultiSelectPanel multiSelectPanel = null;
                 OptionDto[] optionDtos = childDto.getList().get(i);
                 if (null != optionDtos && optionDtos.length > 0) {
 //                    selectItemPanel.getjPanel1().setLayout(new GridLayout(optionDtos.length, 1));
                     for (int j = 0; j < optionDtos.length; j++) {
                         multiSelectPanel = new MultiSelectPanel();
                         selectItemPanel.getMultiSelectPanelList().add(multiSelectPanel);
                         multiSelectPanel.getjCheckBox1().setText(" " + map.get(j) + "  " + optionDtos[j].getName());
                         multiSelectPanel.setId(optionDtos[j].getId());
                         if(!optionMap.containsKey(optionDtos[j].getId()))
                             optionMap.put(optionDtos[j].getId(), map.get(j));
                         multiSelectPanel.getjCheckBox1().setActionCommand(map.get(j));
                         //设置能否编辑选项只有开始投票并且未结束的才能编辑
                         if(null!=isStart&&"true".equals(isStart)&&null!=isClose&&"false".equals(isClose)){
                             multiSelectPanel.getjCheckBox1().setEnabled(true);
                         }else{
                             multiSelectPanel.getjCheckBox1().setEnabled(false);
                         }
                        
                         selectItemPanel.getjPanel1().add(multiSelectPanel);
                     }
                     selectItemPanel.setOptionDtos(optionDtos);
                     selectItemPanel.getjPanel1().validate();
                     selectItemPanel.setItemPanelType(Constants.SELECTITEMTYPE_MULTI);
                 }
             }
             if (childDto.getItemType().equals("" + Constants.SELECTITEMTYPE_SINGLE)) {
                 SingleSelectPanel selectPanel = null;
                 OptionDto[] optionDtos = childDto.getList().get(i);
                 if (null != optionDtos && optionDtos.length > 0) {
 //                    selectItemPanel.getjPanel1().setLayout(new GridLayout(optionDtos.length, 1));
                     for (int j = 0; j < optionDtos.length; j++) {
                         selectPanel = new SingleSelectPanel();
                         selectPanel.getjRadioButton1().setText(" " + map.get(j) + "  " + optionDtos[j].getName());
                         selectPanel.getjRadioButton1().setActionCommand(map.get(j));
                         selectPanel.setId(optionDtos[j].getId());
                         if(!optionMap.containsKey(optionDtos[j].getId()))
                             optionMap.put(optionDtos[j].getId(), map.get(j));
                         selectItemPanel.getjPanel1().add(selectPanel);
                         
                         if(null!=isStart&&"true".equals(isStart)&&null!=isClose&&"false".equals(isClose)){
                             selectPanel.getjRadioButton1().setEnabled(true);
                         }else{
                             selectPanel.getjRadioButton1().setEnabled(false);
                         }
                         selectItemPanel.getSingleSelectPanelList().add(selectPanel);
                         selectItemPanel.getButtonGroup1().add(selectPanel.getjRadioButton1());
                     }
                     selectItemPanel.setOptionDtos(optionDtos);
                     selectItemPanel.getjPanel1().validate();
                     selectItemPanel.setItemPanelType(Constants.SELECTITEMTYPE_SINGLE);
                 }
             }
             if (childDto.getItemType().equals("" + Constants.SELECTITEMTYPE_YES_OR_NO)) {
                         selectItemPanel.getjPanel1().setLayout(new GridLayout(1, 1));
                         YesorNoPanel yesorNoPanel = new YesorNoPanel();
                         OptionDto[] optionDtos = childDto.getList().get(i);
                         if (null != optionDtos && optionDtos.length > 0) {
 //                            selectItemPanel.getjPanel1().setLayout(new GridLayout(optionDtos.length, 1));
                             for (int j = 0; j < optionDtos.length; j++) {
                                 if("是".equals(optionDtos[j].getName())){
                                     yesorNoPanel.getYesRadio().setActionCommand(optionDtos[j].getId());
                                     if (!optionMap.containsKey(optionDtos[j].getId())) 
                                          optionMap.put(optionDtos[j].getId(), "A");
                                 }
                                 if("否".equals(optionDtos[j].getName())){
                                     yesorNoPanel.getNoRadio().setActionCommand(optionDtos[j].getId());
                                     if (!optionMap.containsKey(optionDtos[j].getId())) 
                                          optionMap.put(optionDtos[j].getId(), "B");
                                 }
                                  if(null!=isStart&&"true".equals(isStart)&&null!=isClose&&"false".equals(isClose)){
                                      yesorNoPanel.getNoRadio().setEnabled(true);
                                      yesorNoPanel.getYesRadio().setEnabled(true);
                                  }else{
                                      yesorNoPanel.getNoRadio().setEnabled(false);
                                      yesorNoPanel.getYesRadio().setEnabled(false);
                                  }
                             }
                         }
                         selectItemPanel.getjPanel1().add(yesorNoPanel);
                         selectItemPanel.getYesorNoPanelList().add(yesorNoPanel);
                         selectItemPanel.getjPanel1().validate();
                         selectItemPanel.setItemPanelType(Constants.SELECTITEMTYPE_YES_OR_NO);
                     }
         }
         
         voteTabItemPane.getSelectItemPanelList().add(selectItemPanel);
         voteTabItemPane.getjPanel4().removeAll();
         for (Iterator<SelectItemPanel> it = voteTabItemPane.getSelectItemPanelList().iterator(); it.hasNext();) {
             SelectItemPanel selectItemPanel2 = it.next();
             voteTabItemPane.getjPanel4().add(selectItemPanel2);
         }
       //如果表决项面板少于6，则补一些新空面板上去
 		if(voteTabItemPane.getSelectItemPanelList().size()<6){
 			for(int i=0;i<6-voteTabItemPane.getSelectItemPanelList().size()+3;i++){
 				voteTabItemPane.getjPanel4().add(new JPanel());
 			}
 		}
         voteTabItemPane.validate();
         DataUtil.setValue(DataUtil.Key.optionMap, optionMap);
     }
     public void addSelectItem(VoteDataChildDto childDto,String creator,String isStart,String isClose,VoteTabItemPane voteTabItemPane) throws Exception{
     	initConstMap();
     	VoteTabItemPane mainPanel=voteTabItemPane;
     	SelectItemPanel selectItemPanel = new SelectItemPanel(mainPanel);
     	selectItemPanel.getjLabel1().setText("表决项 " + (mainPanel.getSelectItemPanelList().size() + 1));
     	selectItemPanel.setItemPanelKey(Constants.ITEMKEY + (mainPanel.getSelectItemPanelList().size() + 1));
     	selectItemPanel.getjLabel2().setText(childDto.getTitle());
     	selectItemPanel.setId(childDto.getTitleid());
     	selectItemPanel.getjLabel4().setText(childDto.getTitledescription());
     	LoginInfo info=DataUtil.getValue(DataUtil.Key.LoginInfo);
     	//根据当前登录用户是否创建用户进行判断是否显示修改删除按钮
     	if(null!=creator&&!info.getUsername().equals(creator)){
     		selectItemPanel.getjButton1().setVisible(false);
     		selectItemPanel.getjButton2().setVisible(false);
     	}
     	//判断按钮是否可用
     	if("false".equals(isStart)&&"false".equals(isClose)){
     		selectItemPanel.getjButton1().setEnabled(true);
     		selectItemPanel.getjButton2().setEnabled(true);
     	}else{
     		selectItemPanel.getjButton1().setEnabled(false);
     		selectItemPanel.getjButton2().setEnabled(false);
     	}
     	for (int i = 0; i < childDto.getList().size(); i++) {
     		selectItemPanel.getjPanel1().setLayout(new BoxLayout(selectItemPanel.getjPanel1(), BoxLayout.Y_AXIS));
     		if (childDto.getItemType().equals("" + Constants.SELECTITEMTYPE_MULTI)) {
     			MultiSelectPanel multiSelectPanel = null;
     			OptionDto[] optionDtos = childDto.getList().get(i);
     			if (null != optionDtos && optionDtos.length > 0) {
 //    				selectItemPanel.getjPanel1().setLayout(new GridLayout(optionDtos.length, 1));
     				for (int j = 0; j < optionDtos.length; j++) {
     					multiSelectPanel = new MultiSelectPanel();
     					selectItemPanel.getMultiSelectPanelList().add(multiSelectPanel);
     					multiSelectPanel.getjCheckBox1().setText(" " + map.get(j) + "  " + optionDtos[j].getName());
     					multiSelectPanel.setId(optionDtos[j].getId());
     					if(!optionMap.containsKey(optionDtos[j].getId()))
     						optionMap.put(optionDtos[j].getId(), map.get(j));
     					multiSelectPanel.getjCheckBox1().setActionCommand(map.get(j));
     					//设置能否编辑选项只有开始投票并且未结束的才能编辑
     					if(null!=isStart&&"true".equals(isStart)&&null!=isClose&&"false".equals(isClose)){
     						multiSelectPanel.getjCheckBox1().setEnabled(true);
     					}else{
     						multiSelectPanel.getjCheckBox1().setEnabled(false);
     					}
     					
     					selectItemPanel.getjPanel1().add(multiSelectPanel);
     				}
     				selectItemPanel.setOptionDtos(optionDtos);
     				selectItemPanel.getjPanel1().validate();
     				selectItemPanel.setItemPanelType(Constants.SELECTITEMTYPE_MULTI);
     			}
     		}
     		if (childDto.getItemType().equals("" + Constants.SELECTITEMTYPE_SINGLE)) {
     			SingleSelectPanel selectPanel = null;
     			OptionDto[] optionDtos = childDto.getList().get(i);
     			if (null != optionDtos && optionDtos.length > 0) {
 //    				selectItemPanel.getjPanel1().setLayout(new GridLayout(optionDtos.length, 1));
     				for (int j = 0; j < optionDtos.length; j++) {
     					selectPanel = new SingleSelectPanel();
     					selectPanel.getjRadioButton1().setText(" " + map.get(j) + "  " + optionDtos[j].getName());
     					selectPanel.getjRadioButton1().setActionCommand(map.get(j));
     					selectPanel.setId(optionDtos[j].getId());
     					if(!optionMap.containsKey(optionDtos[j].getId()))
     						optionMap.put(optionDtos[j].getId(), map.get(j));
     					selectItemPanel.getjPanel1().add(selectPanel);
     					
     					if(null!=isStart&&"true".equals(isStart)&&null!=isClose&&"false".equals(isClose)){
     						selectPanel.getjRadioButton1().setEnabled(true);
     					}else{
     						selectPanel.getjRadioButton1().setEnabled(false);
     					}
     					selectItemPanel.getSingleSelectPanelList().add(selectPanel);
     					selectItemPanel.getButtonGroup1().add(selectPanel.getjRadioButton1());
     				}
     				selectItemPanel.setOptionDtos(optionDtos);
     				selectItemPanel.getjPanel1().validate();
     				selectItemPanel.setItemPanelType(Constants.SELECTITEMTYPE_SINGLE);
     			}
     		}
     		if (childDto.getItemType().equals("" + Constants.SELECTITEMTYPE_YES_OR_NO)) {
     			selectItemPanel.getjPanel1().setLayout(new GridLayout(1, 1));
     			YesorNoPanel yesorNoPanel = new YesorNoPanel();
     			OptionDto[] optionDtos = childDto.getList().get(i);
     			if (null != optionDtos && optionDtos.length > 0) {
 //    				selectItemPanel.getjPanel1().setLayout(new GridLayout(optionDtos.length, 1));
     				for (int j = 0; j < optionDtos.length; j++) {
     					if("是".equals(optionDtos[j].getName())){
     						yesorNoPanel.getYesRadio().setActionCommand(optionDtos[j].getId());
     						if (!optionMap.containsKey(optionDtos[j].getId())) 
     							optionMap.put(optionDtos[j].getId(), "A");
     					}
     					if("否".equals(optionDtos[j].getName())){
     						yesorNoPanel.getNoRadio().setActionCommand(optionDtos[j].getId());
     						if (!optionMap.containsKey(optionDtos[j].getId())) 
     							optionMap.put(optionDtos[j].getId(), "B");
     					}
     					if(null!=isStart&&"true".equals(isStart)&&null!=isClose&&"false".equals(isClose)){
     						yesorNoPanel.getNoRadio().setEnabled(true);
     						yesorNoPanel.getYesRadio().setEnabled(true);
     					}else{
     						yesorNoPanel.getNoRadio().setEnabled(false);
     						yesorNoPanel.getYesRadio().setEnabled(false);
     					}
     				}
     			}
     			selectItemPanel.getjPanel1().add(yesorNoPanel);
     			selectItemPanel.getYesorNoPanelList().add(yesorNoPanel);
     			selectItemPanel.getjPanel1().validate();
     			selectItemPanel.setItemPanelType(Constants.SELECTITEMTYPE_YES_OR_NO);
     		}
     	}
     	
     	mainPanel.getSelectItemPanelList().add(selectItemPanel);
     	mainPanel.getjPanel4().removeAll();
     	for (Iterator<SelectItemPanel> it = mainPanel.getSelectItemPanelList().iterator(); it.hasNext();) {
     		SelectItemPanel selectItemPanel2 = it.next();
     		mainPanel.getjPanel4().add(selectItemPanel2);
     	}
     	//如果表决项面板少于6，则补一些新空面板上去
 		if(mainPanel.getSelectItemPanelList().size()<6){
 			for(int i=0;i<6-mainPanel.getSelectItemPanelList().size()+3;i++){
 				mainPanel.getjPanel4().add(new JPanel());
 			}
 		}
     	mainPanel.validate();
     	DataUtil.setValue(DataUtil.Key.optionMap, optionMap);
     }
     //修改表决项
     public void update_selectSuccess(XmlUtilParse util) {
         VoteDataChildDto childDto = new VoteDataChildDto();
         String selectItemType = util.getElementValue("type") == null ? null : util.getElementValue("type").toString();
         if (null != selectItemType && !"1".equals(selectItemType) && !"2".equals(selectItemType)) {
             selectItemType = "0";//如果不是单选，不是多选，就默认是是否选项
         }
         childDto.setItemType("" + selectItemType);
         String title = util.getElementValue("select_title") == null ? null : util.getElementValue("select_title").toString();
         childDto.setTitle(title);
         String title_descrition = util.getElementValue("select_remark") == null ? null : util.getElementValue("select_remark").toString();
         childDto.setTitledescription(title_descrition);
 
         Object o = util.getElementValue("id");
         if (o instanceof List) {
             List ids = (ArrayList) (util.getElementValue("id") == null ? null : util.getElementValue("id"));
             List names = (ArrayList) (util.getElementValue("option_name") == null ? null : util.getElementValue("option_name"));
             OptionDto[] answer = new OptionDto[ids.size() - 1];
             childDto.setTitleid(ids.get(0).toString());
             for (int i = 0; i < ids.size() - 1; i++) {
                 OptionDto optionDto = new OptionDto();
                 optionDto.setId(ids.get(i + 1).toString());
                 optionDto.setName(names.get(i).toString());
                 answer[i] = optionDto;
             }
             childDto.getList().add(answer);
         }
         if (o instanceof String) {
             String id = util.getElementValue("id") == null ? null : util.getElementValue("id").toString();
             childDto.setTitleid(id);
         }
         updateSelectedItem(childDto);
     }
     
     public void updateSelectedItem(VoteDataChildDto childDto){
         try {
             initConstMap();
             VoteTabItemPane voteTabItemPane=(VoteTabItemPane)mainPanel.getjTabbedPane1().getSelectedComponent();
             voteTabItemPane.getjPanel4().removeAll();
             for (Iterator<SelectItemPanel> it = voteTabItemPane.getSelectItemPanelList().iterator(); it.hasNext();) {
                 SelectItemPanel selectItemPanel = it.next();
                 if (null != voteTabItemPane.getSelectedItemPanel() && voteTabItemPane.getSelectedItemPanel().getId() == selectItemPanel.getId()) {
                     selectItemPanel.getjLabel2().setText(childDto.getTitle());
                     selectItemPanel.getjLabel4().setText(childDto.getTitledescription());
                     selectItemPanel.getjPanel1().removeAll();
                     selectItemPanel.setId(childDto.getTitleid());
                     for (int i = 0; i < childDto.getList().size(); i++) {
                     	selectItemPanel.getjPanel1().setLayout(new BoxLayout(selectItemPanel.getjPanel1(), BoxLayout.Y_AXIS));
                         if (childDto.getItemType().equals("" + Constants.SELECTITEMTYPE_MULTI)) {
                             MultiSelectPanel multiSelectPanel = null;
                             OptionDto[] optionDtos = childDto.getList().get(i);
                             if (null != optionDtos && optionDtos.length > 0) {
 //                                selectItemPanel.getjPanel1().setLayout(new GridLayout(optionDtos.length, 1));
                                 for (int j = 0; j < optionDtos.length; j++) {
                                     multiSelectPanel = new MultiSelectPanel();
                                     selectItemPanel.getMultiSelectPanelList().add(multiSelectPanel);
                                     multiSelectPanel.setId(optionDtos[j].getId());
                                     multiSelectPanel.getjCheckBox1().setText(" " + map.get(j) + "  " + optionDtos[j].getName());
                                     multiSelectPanel.getjCheckBox1().setActionCommand(map.get(j));
                                     if (!optionMap.containsKey(optionDtos[j].getId())) {
                                         optionMap.put(optionDtos[j].getId(), map.get(j));
                                     }else{
                                         optionMap.remove(optionDtos[j].getId());
                                         optionMap.put(optionDtos[j].getId(), map.get(j));
                                     }
                                     selectItemPanel.getjPanel1().add(multiSelectPanel);
                                 }
                                 selectItemPanel.setOptionDtos(optionDtos);
                                 selectItemPanel.getjPanel1().validate();
                                 selectItemPanel.setItemPanelType(Constants.SELECTITEMTYPE_MULTI);
                             }
                         }
                         if (childDto.getItemType().equals("" + Constants.SELECTITEMTYPE_SINGLE)) {
                             SingleSelectPanel selectPanel = null;
                            OptionDto[] optionDtos = childDto.getList().get(i);
                             if (null != optionDtos && optionDtos.length > 0) {
 //                                selectItemPanel.getjPanel1().setLayout(new GridLayout(optionDtos.length, 1));
                                 for (int j = 0; j < optionDtos.length; j++) {
                                     selectPanel = new SingleSelectPanel();
                                     selectPanel.setId(optionDtos[j].getId());
                                     selectPanel.getjRadioButton1().setText(" " + map.get(j) + "  " + optionDtos[j].getName());
                                     selectPanel.getjRadioButton1().setActionCommand(map.get(j));
                                     if (!optionMap.containsKey(optionDtos[j].getId())) {
                                         optionMap.put(optionDtos[j].getId(), map.get(j));
                                     }else{
                                         optionMap.remove(optionDtos[j].getId());
                                         optionMap.put(optionDtos[j].getId(), map.get(j));
                                     }
                                     selectItemPanel.getjPanel1().add(selectPanel);
                                     selectItemPanel.getSingleSelectPanelList().add(selectPanel);
                                     selectItemPanel.getButtonGroup1().add(selectPanel.getjRadioButton1());
                                 }
                                 selectItemPanel.setOptionDtos(optionDtos);
                                 selectItemPanel.getjPanel1().validate();
                                 selectItemPanel.setItemPanelType(Constants.SELECTITEMTYPE_SINGLE);
                             }
                         }
                     if (childDto.getItemType().equals("" + Constants.SELECTITEMTYPE_YES_OR_NO)) {
                         selectItemPanel.getjPanel1().setLayout(new GridLayout(1, 1));
                         YesorNoPanel yesorNoPanel = new YesorNoPanel();
                         OptionDto[] optionDtos = childDto.getList().get(i);
                         if (null != optionDtos && optionDtos.length > 0) {
 //                            selectItemPanel.getjPanel1().setLayout(new GridLayout(optionDtos.length, 1));
                             for (int j = 0; j < optionDtos.length; j++) {
                                 if("是".equals(optionDtos[j].getName())){
                                     yesorNoPanel.getYesRadio().setActionCommand(optionDtos[j].getId());
                                     if (!optionMap.containsKey(optionDtos[j].getId())) {
                                         optionMap.put(optionDtos[j].getId(), "A");
                                     }else{
                                         optionMap.remove(optionDtos[j].getId());
                                         optionMap.put(optionDtos[j].getId(), "A");
                                     }
                                 }
                                 if("否".equals(optionDtos[j].getName())){
                                     yesorNoPanel.getNoRadio().setActionCommand(optionDtos[j].getId());
                                     if (!optionMap.containsKey(optionDtos[j].getId())) {
                                         optionMap.put(optionDtos[j].getId(), "B");
                                     }else{
                                         optionMap.remove(optionDtos[j].getId());
                                         optionMap.put(optionDtos[j].getId(), "B");
                                     }
                                 }
                             }
                         }
                         selectItemPanel.getjPanel1().add(yesorNoPanel);
                         selectItemPanel.getYesorNoPanelList().add(yesorNoPanel);
                         selectItemPanel.getjPanel1().validate();
                         selectItemPanel.setItemPanelType(Constants.SELECTITEMTYPE_YES_OR_NO);
                     }
                     }
                 }
                 voteTabItemPane.getjPanel4().add(selectItemPanel);
             }
           //如果表决项面板少于6，则补一些新空面板上去
     		if(voteTabItemPane.getSelectItemPanelList().size()<6){
     			for(int i=0;i<6-voteTabItemPane.getSelectItemPanelList().size()+3;i++){
     				voteTabItemPane.getjPanel4().add(new JPanel());
     			}
     		}
     		voteTabItemPane.validate();
             
         } catch (Exception ex) {
         	log.error("updateSelectedItem", ex);
         }
     }
     //删除表决项
     public void delete_selectSuccess(XmlUtilParse util){
         try {
             String id=(String) util.getElementValue("id");
             VoteTabItemPane voteTabItemPane=(VoteTabItemPane)mainPanel.getjTabbedPane1().getSelectedComponent();
             for (int i = 0; i < voteTabItemPane.getSelectItemPanelList().size(); i++) {
                 SelectItemPanel selectItemPanel = voteTabItemPane.getSelectItemPanelList().get(i);
                 if (id.equals(selectItemPanel.getId())) {
                 	voteTabItemPane.getSelectItemPanelList().remove(i);
                 }
             }
             voteTabItemPane.getjPanel4().removeAll();
             int count = 1;
             if (voteTabItemPane.getSelectItemPanelList().size() != 0) {
                 for (Iterator<SelectItemPanel> it = voteTabItemPane.getSelectItemPanelList().iterator(); it.hasNext();) {
                     SelectItemPanel selectItemPanel = it.next();
                     selectItemPanel.getjLabel1().setText("表决项 " + count);
                     count++;
                     voteTabItemPane.getjPanel4().add(selectItemPanel);
                 }
                 //如果表决项面板少于6，则补一些新空面板上去
         		if(voteTabItemPane.getSelectItemPanelList().size()<6){
         			for(int i=0;i<6-voteTabItemPane.getSelectItemPanelList().size()+3;i++){
         				voteTabItemPane.getjPanel4().add(new JPanel());
         			}
         		}
             }
             voteTabItemPane.getjPanel4().validate();
             voteTabItemPane.getjPanel4().updateUI();
             
         } catch (Exception ex) {
         	log.error("delete_selectSuccess", ex);
         }
     }
     //统计结果
     public void countSuccess(XmlUtilParse util){
         try {
             List<VoteItemResultBean> itemResultBean=util.getItemResultBeansList();
             Map<String,List<VoteItemResultOptionBean>> resultmap=new HashMap<String, List<VoteItemResultOptionBean>>();
             for (Iterator<VoteItemResultBean> it = itemResultBean.iterator(); it.hasNext();) {
                 VoteItemResultBean voteItemResultBean = it.next();
                 resultmap.put(voteItemResultBean.getItemid(), voteItemResultBean.getOptionSelectedIdList());            
             }
             VoteTabItemPane voteTabItemPane=(VoteTabItemPane)mainPanel.getjTabbedPane1().getSelectedComponent();
             voteTabItemPane.getjPanel4().removeAll();
             for (Iterator<SelectItemPanel> it = voteTabItemPane.getSelectItemPanelList().iterator(); it.hasNext();) {
                 SelectItemPanel selectItemPanel = it.next();
                 selectItemPanel.getjPanel2().setVisible(true);//显示投票结果
                 List<VoteItemResultOptionBean> templist=resultmap.get(selectItemPanel.getId());
                 String resultText="";
                 if(null!=templist)
                 for (Iterator<VoteItemResultOptionBean> it1 = templist.iterator(); it1.hasNext();) {
                     VoteItemResultOptionBean voteItemResultOptionBean = it1.next();
                     String id=voteItemResultOptionBean.getId();
                     String num=voteItemResultOptionBean.getNum();
                     String percent=voteItemResultOptionBean.getPercent();
                     if(null!=optionMap)
                        resultText+="选项 "+optionMap.get(id)+"  票数："+num+"  比例："+percent +"%  ";
                 }
                 selectItemPanel.getjLabel6().setText("<html>"+resultText+"</html>");
                 voteTabItemPane.getjPanel4().add(selectItemPanel);
             }
           //如果表决项面板少于6，则补一些新空面板上去
     		if(voteTabItemPane.getSelectItemPanelList().size()<6){
     			for(int i=0;i<6-voteTabItemPane.getSelectItemPanelList().size()+3;i++){
     				voteTabItemPane.getjPanel4().add(new JPanel());
     			}
     		}
     		voteTabItemPane.validate();
         } catch (Exception ex) {
         	log.error("countSuccess", ex);
         }
     }
     ////提交投票
     public void dovoteSuccess(XmlUtilParse util){
         try {
             JOptionPane.showMessageDialog(mainPanel, "已成功提交！");
         } catch (Exception ex) {
         	log.error("dovoteSuccess", ex);
         }
     }
     
 	private voteMainPanel getVoteMainPanel() {
 		if (null == mainPanel) {
 			try {
 				mainPanel = ViewManager.getComponent(voteMainPanel.class);
 			} catch (Exception e) {
 				e.printStackTrace();
 				log.error("getVoteMainPanel", e);
 			}
 		}
 		return mainPanel;
 	}
     private voteMainPanel mainPanel=null;
     private Map<String,VoteDataDto> voteMap=null;
     //加载投票信息
     public void load_voteSuccess(XmlUtilParse util) {
     	mainPanel = null;
     	mainPanel=getVoteMainPanel();
         List<VoteDataDto> voteDataDtos = util.getVoteDataDtosList();
         if (null != voteMap) {
             voteMap.clear();
         } else {
             voteMap = new HashMap<String, VoteDataDto>();
         }
        loadTabitem=new HashMap<String, VoteTabItemPane>();
         VoteTabItemPane voteTabItemPane=null;
         for (Iterator<VoteDataDto> it = voteDataDtos.iterator(); it.hasNext();) {
             try {
                 VoteDataDto voteDataDto = it.next();
                 if(!loadTabitem.containsKey(voteDataDto.getThemeid())){
                     voteMap.put(voteDataDto.getThemeid(), voteDataDto);
                     voteTabItemPane=new VoteTabItemPane();
                     int count=mainPanel.getjTabbedPane1().getTabCount();
                     mainPanel.getjTabbedPane1().addTab(voteDataDto.getTheme(), voteTabItemPane);
                     mainPanel.getjTabbedPane1().setSelectedIndex(count);
                     load_VotedataByID(voteDataDto,voteTabItemPane);
                     loadTabitem.put(voteDataDto.getThemeid(), voteTabItemPane);
                 }
             } catch (Exception ex) {
             	log.error("load_voteSuccess", ex);
             }
         }
     }
     private Map<String, VoteTabItemPane> loadTabitem=new HashMap<String, VoteTabItemPane>();
     public void load_VotedataByID(ParaUtil paraUtil){
 //        load_Vote(paraUtil);
         load_voteDataToMainPanel(voteMap.get(paraUtil.getValue("voteDataID")));
         if(null!=userOptionSeletedMap&&userOptionSeletedMap.size()>0){
             try {
             	mainPanel.getComitVoteBtn().setEnabled(false);
                 setSelectOptionEdit(false);
             } catch (Exception ex) {
             	log.error("load_VotedataByID", ex);
             }
             }
     }
     public void load_VotedataByID(VoteDataDto voteDataDto,VoteTabItemPane voteTabItemPane){
 //        load_Vote(paraUtil);
     	load_voteDataToMainPanel(voteDataDto,voteTabItemPane);
     	if(null!=userOptionSeletedMap&&userOptionSeletedMap.size()>0){
     		try {
     			mainPanel.getComitVoteBtn().setEnabled(false);
     			setSelectOptionEdit(false);
     		} catch (Exception ex) {
     			log.error("load_VotedataByID", ex);
     		}
     	}
     }
     private  boolean isclose,isstart;
     public void load_voteDataToMainPanel(VoteDataDto voteDataDto) {
     	VoteTabItemPane voteTabItemPane = (VoteTabItemPane)mainPanel.getjTabbedPane1().getSelectedComponent();
         try {
         	voteTabItemPane.getjPanel4().removeAll();
         	voteTabItemPane.getSelectItemPanelList().clear();
         	voteTabItemPane.setId(voteDataDto.getThemeid());
         	voteTabItemPane.getjLabel3().setText(voteDataDto.getTheme());
         	voteTabItemPane.getjLabel4().setText(voteDataDto.getThemeDescription());
         	voteTabItemPane.buttonStatus(voteDataDto.getCreator());
             isclose=true;
             isstart=true;
             if ("false".equals(voteDataDto.getIsClose())) {
                 isclose = false;
             }
             if ("false".equals(voteDataDto.getIsStart())) {
                 isstart = false;
             }
             mainPanel.voteStatus(isstart, isclose);
             if (!isstart) {
             	voteTabItemPane.getjLabel5().setText(StringUtil.getUIString("voteStatus.notstart"));
             }
             if (isstart && !isclose) {
             	voteTabItemPane.getjLabel5().setText(StringUtil.getUIString("voteStatus.starting"));
             }
             if (isclose) {
             	voteTabItemPane.getjLabel5().setText(StringUtil.getUIString("voteStatus.end"));
             }
             List<VoteDataChildDto> childDtos=voteDataDto.getList();
             for (Iterator<VoteDataChildDto> it = childDtos.iterator(); it.hasNext();) {
                 VoteDataChildDto voteDataChildDto = it.next();
                 addSelectItem(voteDataChildDto,voteDataDto.getCreator(),voteDataDto.getIsStart(),voteDataDto.getIsClose());
             }
             checkUserIsCommit(voteDataDto.getThemeid());
             
         } catch (Exception ex) {
         	log.error("load_voteDataToMainPanel", ex);
         }
     }
     public void load_voteDataToMainPanel(VoteDataDto voteDataDto,VoteTabItemPane voteTabItemPane) {
     	try {
     		voteTabItemPane.getjPanel4().removeAll();
     		voteTabItemPane.getSelectItemPanelList().clear();
     		voteTabItemPane.setId(voteDataDto.getThemeid());
     		voteTabItemPane.getjLabel3().setText(voteDataDto.getTheme());
     		voteTabItemPane.getjLabel4().setText(voteDataDto.getThemeDescription());
     		voteTabItemPane.buttonStatus(voteDataDto.getCreator());
     		voteTabItemPane.setIsclose(voteDataDto.getIsClose());
     		voteTabItemPane.setIsstar(voteDataDto.getIsStart());
     		voteTabItemPane.setCreator(voteDataDto.getCreator());
     		voteTabItemPane.setButtonStatus();
     		List<VoteDataChildDto> childDtos=voteDataDto.getList();
     		for (Iterator<VoteDataChildDto> it = childDtos.iterator(); it.hasNext();) {
     			VoteDataChildDto voteDataChildDto = it.next();
     			addSelectItem(voteDataChildDto,voteDataDto.getCreator(),voteDataDto.getIsStart(),voteDataDto.getIsClose(),voteTabItemPane);
     		}
     		//如果表决项面板少于6，则补一些新空面板上去  chard 20120529
 //    		if(voteTabItemPane.getSelectItemPanelList().size()<6){
 //    			for(int i=0;i<6-voteTabItemPane.getSelectItemPanelList().size()+3;i++){
 //    				voteTabItemPane.getjPanel4().add(new JPanel());
 //    			}
 //    			voteTabItemPane.getjPanel4().validate();
 //    		}
     		checkUserIsCommit(voteDataDto.getThemeid());
     		
     	} catch (Exception ex) {
     		log.error("load_voteDataToMainPanel", ex);
     	}
     }
     
     public void checkUserIsCommit(String id)throws Exception{
                     
             //加载用户已经提交选项信息
             LoginInfo info=DataUtil.getValue(DataUtil.Key.LoginInfo);
             String user_id=info.getUsername();
             String themeid=id;
              Connection conn = DataUtil.getValue(DataUtil.Key.Connection);
             String roomjid=info.getConfno() + "@conference." + conn.getServiceName();
             search_Voted_record(ParaUtil.newInstance().setString("themeid", themeid).setString("user_id", user_id).setString("roomjid", roomjid));
     }
     
     
     public void setSelectOptionEdit(boolean flag){
     	VoteTabItemPane voteTabItemPane=(VoteTabItemPane)mainPanel.getjTabbedPane1().getSelectedComponent();
         try {
             if(null!=voteTabItemPane.getSelectItemPanelList())
             for (Iterator<SelectItemPanel> it = voteTabItemPane.getSelectItemPanelList().iterator(); it.hasNext();) {
                 SelectItemPanel selectItemPanel = it.next();
 //                selectItemPanel.getjButton1().setEnabled(false);
 //                selectItemPanel.getjButton2().setEnabled(false);
                 List<MultiSelectPanel> mulList= selectItemPanel.getMultiSelectPanelList();
                 if (null != mulList) {
                     for (Iterator<MultiSelectPanel> it1 = mulList.iterator(); it1.hasNext();) {
                         MultiSelectPanel multiSelectPanel = it1.next();
                         multiSelectPanel.getjCheckBox1().setEnabled(flag);
                          if(userOptionSeletedMap.containsKey(multiSelectPanel.getId()))
                             multiSelectPanel.getjCheckBox1().setSelected(true);
                     }
                 }
                 List<SingleSelectPanel> singleList=selectItemPanel.getSingleSelectPanelList();
                 if(null!=singleList){
                     for (Iterator<SingleSelectPanel> it1 = singleList.iterator(); it1.hasNext();) {
                         SingleSelectPanel singleSelectPanel = it1.next();
                         singleSelectPanel.getjRadioButton1().setEnabled(flag);
                         if(userOptionSeletedMap.containsKey(singleSelectPanel.getId()))
                             singleSelectPanel.getjRadioButton1().setSelected(true);
                     }
                 }
                 List<YesorNoPanel> yesorNoPanelsList=selectItemPanel.getYesorNoPanelList();
                 if(null!=yesorNoPanelsList){
                     for (Iterator<YesorNoPanel> it1 = yesorNoPanelsList.iterator(); it1.hasNext();) {
                         YesorNoPanel yesorNoPanel = it1.next();
                         yesorNoPanel.getYesRadio().setEnabled(flag);
                         yesorNoPanel.getNoRadio().setEnabled(flag);
                         if(userOptionSeletedMap.containsKey(yesorNoPanel.getYesRadio().getActionCommand()))
                             yesorNoPanel.getYesRadio().setSelected(true);
                         if(userOptionSeletedMap.containsKey(yesorNoPanel.getNoRadio().getActionCommand()))
                             yesorNoPanel.getNoRadio().setSelected(true);
                     }
                 }
             }
         } catch (Exception ex) {
         	log.error("setSelectOptionEdit", ex);
         }
     }
     
     
     private Map<Integer,String> map=new HashMap<Integer, String>();
     private void initConstMap(){
           String[] letter={"A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"};
           for (int i = 0; i <letter.length; i++) {
             map.put(i, letter[i]);
         }
     }
     
     private String getSearchVotedRecord(String themeid){
         LoginInfo info = DataUtil.getValue(DataUtil.Key.LoginInfo);
         String user_id = info.getUsername();
         Connection conn = DataUtil.getValue(DataUtil.Key.Connection);
         String roomjid = info.getConfno() + "@conference." + conn.getServiceName();
         StringBuffer buffer=new StringBuffer();
         buffer.append("<operate name=\"search_vote_record\">");
         buffer.append("<id>"+themeid+"</id>");
         buffer.append("<roomjid>"+roomjid+"</roomjid>");
         buffer.append("<user_id>"+user_id+"</user_id>");
         buffer.append("</operate>");
         return buffer.toString();
     }
     
     private Map<String,String> userOptionSeletedMap=null;
     public void search_vote_recordSuccess(XmlUtilParse util){
         if(null!=userOptionSeletedMap)
             userOptionSeletedMap.clear();
         userOptionSeletedMap = util.getVotedOptionRecordMap();
         if (null != userOptionSeletedMap && userOptionSeletedMap.size() > 0) {
             try {
                 mainPanel.getComitVoteBtn().setEnabled(false);
                 setSelectOptionEdit(false);
             } catch (Exception ex) {
             	log.error("search_vote_recordSuccess", ex);
             }
         }else{
             if(!isclose&&isstart){
                 try {
                     mainPanel.getComitVoteBtn().setEnabled(true);
                     setSelectOptionEdit(true);
                 } catch (Exception ex) {
                 	log.error("search_vote_recordSuccess", ex);
                 }
             }
         }
         if (null == util.getVotedOptionRecordMap() || util.getVotedOptionRecordMap().size() == 0) {
             userOptionSeletedMap = new HashMap<String, String>();
         }
     }
     
     public void reconnect()throws Exception{
     	mainPanel.setToolBarEnable(true);
     	loadTabitem.clear();
     	mainPanel.reloadData();
     }
     
     public void disconnect()throws Exception{
     	mainPanel.setToolBarEnable(false);
     }
 }
