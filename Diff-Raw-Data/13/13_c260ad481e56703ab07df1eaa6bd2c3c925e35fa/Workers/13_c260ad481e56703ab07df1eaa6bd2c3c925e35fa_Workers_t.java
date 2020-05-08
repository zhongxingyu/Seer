 package models;
 
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
 import play.modules.morphia.Model;
 
 import com.google.code.morphia.annotations.Entity;
 import com.google.code.morphia.annotations.Reference;
 
 /** 男职工情况登记薄. */
 @Entity
 public class Workers extends Model {
 	// 所属部门
 	@Reference(ignoreMissing = false)
 	public Department department;
 	// 职工姓名
 	public String name;
 	// 汉族人口数
 	public int han;
 	// 少数民族人口数
 	public int minority;
 	// 已婚育龄妇女姓名
 	public String woman;
 	// 已婚育龄妇女出生年月
 	public Date womanDate;
 	// 结婚时间
 	public Date weddingTime;
 	// 户口性质
 	public String type;
 	// 子女数-男
 	public int boy;
 	// 子女数-女
 	public int girl;
 	// 子女出生日期-最小
 	public Date childrenMinDate;
 	// 子女出生日期-最大
 	public Date childrenMaxDate;
 	// 禁忌症
 	public String contraindication;
 	// 节育情况
 	public String measure;
 	// 节育时间
 	public Date measureDate;
 	// 家庭地址
 	public String live;
 	// 备注
 	public String notes;
 	// 创建时间
 	public Date createAt;
 	// 修改时间
 	public Date modifyAt;
 	
 	/** json序列化. */
 	public Map<String, Object> serialize() {
 		Map<String, Object> result = new HashMap<String, Object>();
 		result.put("id", this.getId().toString());
 		result.put("live", this.live);
 		result.put("name", this.name);
 		result.put("woman", this.woman);
 		result.put("type", this.type);
 		result.put("han", this.han);
 		result.put("minority", this.minority);
 		result.put("boy", this.boy);
 		result.put("girl", this.girl);
 		result.put("contraindication", this.contraindication);
 		result.put("measure", this.measure);
		result.put("measureDate", this.measureDate == null ? "" : utils.Utils.formatDate(this.measureDate));
 		result.put("notes", this.notes);
 		result.put("department", this.department != null ? this.department.serialize() : "");
 		result.put("departmentName", this.department != null ? this.department.name : "");
 		result.put("womanDate", this.womanDate == null ? "" : utils.Utils.formatDate(this.womanDate));
 		result.put("weddingTime", this.weddingTime == null ? null : utils.Utils.formatDate(this.weddingTime));
 		result.put("childrenMinDate", this.childrenMinDate == null ? "" : utils.Utils.formatDate(this.childrenMinDate));
 		result.put("childrenMaxDate", this.childrenMaxDate == null ? "" : utils.Utils.formatDate(this.childrenMaxDate));
 		result.put("createAt", utils.Utils.formatDate(this.createAt));
 		result.put("modifyAt", utils.Utils.formatDate(this.modifyAt));
 		return result;
 	}
 }
