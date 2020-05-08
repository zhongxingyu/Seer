 
 package actions;
 
 import java.util.ArrayList;
 import java.util.Date;
 
 import org.hibernate.annotations.Tables;
 
 import tables.Resortremark;
 import tables.Ressupplement;
 import tables.Userinfo;
 
 import dao.ResortDao;
 import dao.ResortremarkDao;
 import dao.RessupplementDao;
 
 public class Resort extends BaseAction {
     
     static final int pageSize = 12;
     
     static class ResortPage {
         public int page;
     }
     public String list() {
         ResortPage param = (ResortPage) getParam(ResortPage.class);
         ResortDao dao = new ResortDao();
         return jsonResult(dao.queryByPage(pageSize, param.page));
     }
 
     
     public static class SearchResortByNameParam{
     	String searchText;
     }
     
     public String searchResortByName(){
     	SearchResortByNameParam param = (SearchResortByNameParam) getParam(SearchResortByNameParam.class);
     	ResortDao rd = new ResortDao();
     	ArrayList<tables.Resort> list = rd.findResortLikename(param.searchText);
     	return jsonResult(list);
     }
     
     
     public static class FindResortByNameParam{
     	String resortName;
     }
     
     public String findResortByName(){
     	FindResortByNameParam param = (FindResortByNameParam) getParam(FindResortByNameParam.class);
     	ResortDao rd = new ResortDao();
     	tables.Resort r = rd.findResortByname(param.resortName);
     	return jsonResult(r);
     }
     
     
     public static class GetResortByIdParam{
     	int resortId;
     }
     
     public String getResortById (){
     	GetResortByIdParam param = (GetResortByIdParam) getParam(GetResortByIdParam.class);
     	ResortDao rd = new ResortDao();
     	tables.Resort r = rd.findResortById(param.resortId);    	
     	
     	return jsonResult(r);
 	}
     
     
     public static class GetRessupplementsByResortIdParam{
     	int resortId;
     }
     
     public String getRessupplementsByResortId(){
     	GetRessupplementsByResortIdParam param = (GetRessupplementsByResortIdParam) getParam(GetRessupplementsByResortIdParam.class);
     	ResortDao rd = new ResortDao();
     	ArrayList<Ressupplement> rs = null;
     	rs = rd.findResSuplementByid(param.resortId);
     	return jsonResult(rs);
     }
     
     
     public static class EditResortParam{
     	int resortId;
     	String resPicture;
     	String resName;
     	String resAddress;
     	String resLabel;
     }
     
     public String editResort(){
     	EditResortParam param = (EditResortParam) getParam(EditResortParam.class);
     	if(param.resName == null || param.resName.equals("")){
     		return jsonResult("resName");
     	}
     	if(param.resAddress == null || param.resAddress.equals("")){
     		return jsonResult("resAddress");
     	}
     	
     	ResortDao rd = new ResortDao();
     	tables.Resort rs;
     	rs = rd.findResortById(param.resortId);
     	if(rs == null){
     		return jsonResult("resortId");
     	}
     	rs.setResName(param.resName);
     	rs.setResPicture(param.resPicture);
     	rs.setResAddress(param.resAddress);
     	rs.setResLabel(param.resLabel);
     	rd.updateResort(rs);
     	
     	return jsonResult("ok");
     }
     
     
     public static class EditResspupplementParam{
     	Ressupplement ressupplement;
     }
     
     public String editResspupplement(){
     	EditResspupplementParam param = (EditResspupplementParam) getParam(EditResspupplementParam.class);
     	if (param.ressupplement.getResHeadline() == null || param.ressupplement.getResHeadline().equals("")){
     		return jsonResult("resHeadline");
     	}
     	if (param.ressupplement.getResContent() == null || param.ressupplement.getResContent().equals("")){
     		return jsonResult("resContent");
     	}
     	
     	RessupplementDao rsd = new RessupplementDao();
     	Ressupplement rs = null;
     	rs = rsd.findRessupplementbyid(param.ressupplement.getResSupplementId());
     	if (rs == null){
     		return jsonResult("resSupplementId");
     	}
     	
     	rs.setResHeadline(param.ressupplement.getResHeadline());
     	rs.setResContent(param.ressupplement.getResContent());
    	rsd.updateRessupplement(rs);
     	return jsonResult("ok");
     }
     
     
     public static class AddResortParam{
     	tables.Resort resort;
     }
     
     public String addResort(){
     	AddResortParam param = (AddResortParam) getParam(AddResortParam.class);
     	if(param.resort == null){
     		return jsonResult("resort");
     	}
     	
     	param.resort.setResortId(null);
     	
     	if(param.resort.getResName() == null || param.resort.getResName().equals("")){
     		return jsonResult("resort.resName");
     	}
     	if(param.resort.getResAddress() == null || param.resort.getResAddress().equals("")){
     		return jsonResult("resort.resAddress");
     	}
     	if(param.resort.getResLabel() == null || param.resort.getResLabel().equals("")){
     		return jsonResult("resort.resLabel");
     	}
         param.resort.setResState("check pending");
     	
     	ResortDao rd = new ResortDao();
     	tables.Resort r = rd.findResortByname(param.resort.getResName());
     	if(r != null){
     		return jsonResult("resort.resName");
     	}
     	rd.addResort(param.resort);    	
     	return jsonResult("ok");
     }
     
     
     public static class AddRessupplementParam{
     	Ressupplement ressupplement;
     }
     
     public String addRessupplement(){
     	AddRessupplementParam param = (AddRessupplementParam) getParam(AddRessupplementParam.class);
     	if(param.ressupplement == null){
     		return jsonResult("ressupplement");
     	}
     	
     	param.ressupplement.setResSupplementId(null);
     	
     	if(param.ressupplement.getResortId() == null || param.ressupplement.getResortId().equals("")){
     		return jsonResult("ressupplement.resortId");
     	}
     	if(param.ressupplement.getResHeadline() == null || param.ressupplement.getResHeadline().equals("")){
     		return jsonResult("ressupplement.resHeadline");
     	}
     	if(param.ressupplement.getResContent() == null || param.ressupplement.getResContent().equals("")){
     		return jsonResult("ressupplement.resContent");
     	}
         param.ressupplement.setResAuthor(((Userinfo) session("myUserinfo")).getUid());
         param.ressupplement.setResDate(new Date());
     	
     	RessupplementDao rsd = new RessupplementDao();
     	rsd.addRessupplement(param.ressupplement);   	
     	
     	return jsonResult("ok");
     }
     
     
     public static class DeleteResortParam{
     	int resortId;
     }
     
     public String deleteResort(){
     	DeleteResortParam param = (DeleteResortParam) getParam(DeleteResortParam.class);
     	ResortDao rd = new ResortDao();
     	tables.Resort r = rd.findResortById(param.resortId);
     	if(r == null){
     		return jsonResult("resortId");
     	}
     	
     	
     	RessupplementDao rsd = new RessupplementDao();
     	
     	ArrayList<Ressupplement> rs = rsd.findRessupplementbyResortId(r.getResortId());
     	if(rs != null){
 	    	for(Ressupplement ressupplement : rs){
 	    		rsd.deleteRessupplement(ressupplement);
 	    	}
     	}
     	
     	rd.deleteResort(r);
     	return jsonResult("ok");
     }
     
     
     public static class DeleteRessupplementParam{
     	int resSupplementId;
     }
     
     public String deleteRessupplement(){
     	DeleteRessupplementParam param = (DeleteRessupplementParam) getParam(DeleteRessupplementParam.class);
     	RessupplementDao rsd = new RessupplementDao();
     	tables.Ressupplement rs = rsd.findRessupplementbyid(param.resSupplementId);
     	if(rs == null){
     		return jsonResult("resortId");
     	}
     	rsd.deleteRessupplement(rs);
     	return jsonResult("ok");
     }
     
     
     public static class GetResortRemarkByResortIdParam{
     	int resortId;
     }
     
     public String getResortRemarkByResortId(){
     	GetResortRemarkByResortIdParam param = (GetResortRemarkByResortIdParam) getParam(GetResortRemarkByResortIdParam.class);
     	ResortremarkDao rd = new ResortremarkDao();
     	ArrayList<Resortremark> rr = null;
     	rr = rd.findResortremarkByid(param.resortId);
     	int num = rr.size();
     	java.util.Iterator<Resortremark> i = rr.iterator();
     	int mark = 0;
     	while (i.hasNext()) {
 			Resortremark r = i.next();
 			mark += r.getResRemark();
 		}
     	int mark2 = mark / rr.size();
     	return jsonResult(mark2);
     }
     
     
     public static class AddTheResortremarkByResortIdParam{
     	int resortId;
     	int resRemark;
     }
     
     public String addTheResortremarkByResort(){
     	AddTheResortremarkByResortIdParam param = (AddTheResortremarkByResortIdParam) getParam(AddTheResortremarkByResortIdParam.class);
     	ResortremarkDao rrd = new ResortremarkDao();
     	ArrayList<Resortremark> rrList = null;
     	rrList = rrd.findResortremarkByid(param.resortId);
     	if(rrList.size() == 0){
     		return jsonResult("resortId");
     	}
     	Resortremark rr = new Resortremark();
     	Userinfo ui = (Userinfo)session("myUserinfo");
     	rr.setAuthorId(ui.getUid());
     	rr.setResortId(param.resortId);
     	rr.setResortRemarkId(null);
     	rr.setResRemark(param.resRemark);
     	rrd.addResortremark(rr);
     	
     	return jsonResult("ok");
     }
     
     
     public static class FindRelatedResortParam{
     	int resortId;
     }
     
     public String findRelatedResort(){
     	FindRelatedResortParam param = (FindRelatedResortParam) getParam(FindRelatedResortParam.class);
     	ResortDao rd = new ResortDao();
     	ArrayList<tables.Resort> rList = null;
     	tables.Resort r = rd.findResortById(param.resortId);
     	String labels = r.getResLabel();
     	if(labels != null){
     		rList = rd.findResortLikeLabels(labels);
     	}
     	return jsonResult(rList);
     }
 }
