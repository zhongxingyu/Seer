 package biz.thaicom.eBudgeting.models.pln;
 
 public enum ObjectiveTypeId {
 	ROOT(100L), แผนงาน(101L), ผลผลิตโครงการ(102L), กิจกรรมหลัก(103L), 
 	กิจกรรมรอง(104L), กิจกรรมย่อย(105L), กิจกรรมเสริม(106L),กิจกรรมสนับสนุน (107L),
 	
 	ยุทธศาสตร์การจัดสรรงบประมาณ (109L),
 	ประเด็นยุทธศาสตร์(121),
 	เป้าหมายเชิงยุทธศาสตร์(110),
 	เป้าหมายบริการกระทรวง(111),
 	เป้าหมายบริการหน่วยงาน(112),
 	เป้าประสงค์เชิงนโยบาย(113),
 	ยุทธศาสตร์กระทรวง(114),
 	กลยุทธ์หน่วยงาน(115),
 	กลยุทธ์วิธีการหน่วยงาน(116),
 	แนวทางการจัดสรรงบประมาณ(118),
 	วิสัยทัศน์(119),
 	พันธกิจ(120);
 	
 	 private final long id;
 	 private ObjectiveTypeId(long id) {
 		this.id = id;
 	 }
 	 public long getValue() { return id; }
 	 
 	 public String getName() {
 		if(id==100) {
 			return "ROOT";
 		} else if(id==101){
 			return "แผนงาน";
 		} else if(id==102){
 			return "ผลผลิต/โครงการ";
 		} else if(id==103){
 			return "กิจกรรมหลัก";
 		} else if(id==104){
 			return "กิจกรรมรอง";
 		} else if(id==105){
 			return "กิจกรรมย่อย";
 		} else if(id==106){
 			return "กิจกรรมเสริม";
 		} else if(id==107){
 			return "กิจกรรมสนับสนุน";
 		} else if(id==108){
 			return "กิจกรรมรายละเอียด";
 		} else if(id==109){
 			return "ยุทธศาสตร์การจัดสรรงบประมาณ";
 		} else if(id==110) {
 			return "เป้าหมายเชิงยุทธศาสตร์";
 		} else if(id==111) {
 			return "เป้าหมายบริการกระทรวง";
 		} else if(id==112) {
 			return "เป้าหมายบริการหน่วยงาน";
 		} else if(id==113) {
 			return "เป้าประสงค์(เป้าหมาย)เชิงนโยบาย";
 		} else if(id==114) {
 			return "ยุทธศาสตร์กระทรวง";
 		} else if(id==115) {
 			return "กลยุทธ์หน่วยงาน";
 		} else if(id==116) {
 			return "กลยุทธ์/วิธีการกรมฯ";
 		} else if(id==117) {
 			return "";
 		} else if(id==118) {
			return "แนวทางการจัดสรรงบประมาณ(กลยุทธ์หลัก)";
 		} else if(id==119) {
 			return "วิสัยทัศน์";
 		} else if(id==120) {
 			return "พันธกิจ";
 		} else if(id==121) {
 			return "ประเด็นยุทธศาสตร์";
 		}
 		
 		return "undefined";
 	 }
 	 
 }
