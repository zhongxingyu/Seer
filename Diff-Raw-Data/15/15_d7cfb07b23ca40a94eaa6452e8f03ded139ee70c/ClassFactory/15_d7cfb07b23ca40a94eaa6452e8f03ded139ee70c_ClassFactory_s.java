 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 public class ClassFactory implements Serializable {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private int maxCls = 20;
 	private int clsID = 1;
 	private int maxStdPerCls = 5;
 	public final String noFitMath = "There are no suitable math classes for this student.";
 	public final String noFitLA = "There are no suitable language art classes for this student.";
 	public final String noFitRead = "There are no suitable reading classes for this student.";
 
 	List<Classes> mathClsLst;
 	List<Classes> laClsLst;
 	List<Classes> readClsLst;
 	List<Classes> homeroomClsLst;
 	List<Classes> specialClsLst;
 	List<Students> unlucky;
 
 	public ClassFactory() {
 		mathClsLst = new ArrayList<Classes>();
 		laClsLst = new ArrayList<Classes>();
 		readClsLst = new ArrayList<Classes>();
 		homeroomClsLst = new ArrayList<Classes>();
 		specialClsLst = new ArrayList<Classes>();
 		unlucky = new ArrayList<Students>();
 	}
 
 	/**
 	 * Check if std's behavior level fit with class.
 	 * 
 	 * @param std
 	 * @param cls
 	 * @return
 	 */
 	public boolean BLfit(Students std, Classes cls) {
 		if (cls.getBL3() == 1 && std.getBL() == 3) {
 			return false;
 		}
 		if (cls.getBL2() == 3 && (std.getBL() == 2 || std.getBL() == 3)) {
 			return false;
 		}
 		return true;
 	}
 
 	public Classes createClass(String name, int lvl) {
 		return new Classes(name, lvl, clsID++);
 	}
 
 	public int getTotalClasses() {
 		return mathClsLst.size() + laClsLst.size() + readClsLst.size()
 				+ homeroomClsLst.size() + specialClsLst.size();
 	}
 
 	public int getTotalMath() {
 		return mathClsLst.size();
 	}
 
 	public int getTotalLA() {
 		return laClsLst.size();
 	}
 
 	public int getTotalRead() {
 		return readClsLst.size();
 	}
 
 	public int getTotalHomeroom() {
 		return homeroomClsLst.size();
 	}
 
 	public int getTotalSpecial() {
 		return specialClsLst.size();
 	}
 
 	/**
 	 * Check if a student can be assigned to a particular class
 	 * 
 	 * @param std
 	 * @param cls
 	 * @return True if yes, false otherwise.
 	 */
 	public boolean compatible(Students std, Classes cls) {
 		if (!BLfit(std, cls)) {
 			return false;
 		}
 
 		if (cls.getClsName().equals("math")) {
 			if (cls.getLvl() == std.getMath()
 					&& Math.abs(cls.getLowestAge() - std.getAge()) < 4.0
 					&& cls.getTotal() < maxStdPerCls) {
 				return true;
 			} else {
 				return false;
 			}
 		} else if (cls.getClsName().equals("la")) {
 			if (cls.getLvl() == std.getLA()
 					&& Math.abs(cls.getLowestAge() - std.getAge()) < 4.0
 					&& cls.getTotal() < maxStdPerCls) {
 				return true;
 			} else {
 				return false;
 			}
 		} else if (cls.getClsName().equals("read")) {
 			if (cls.getLvl() == std.getRead()
 					&& Math.abs(cls.getLowestAge() - std.getAge()) < 4.0
 					&& cls.getTotal() < maxStdPerCls) {
 				return true;
 			} else {
 				return false;
 			}
 		} else if (cls.getClsName().equals("homeroom")) {
 			if (Math.abs(cls.getLowestAge() - std.getAge()) < 4.0
 					&& cls.getTotal() < maxStdPerCls) {
 				return true;
 			} else {
 				return false;
 			}
 		} else if (cls.getClsName().equals("special")) {
 			if (Math.abs(cls.getLowestAge() - std.getAge()) < 4.0
 					&& cls.getTotal() < maxStdPerCls) {
 				return true;
 			} else {
 				return false;
 			}
 		} else {
 			System.err
 					.println("A Classes object passed into ClassFactory.compatible method is labeled wrong class name");
 			System.err.println("Class passed was named " + cls.getClsName());
 			return false;
 		}
 	}
 
 	/**
 	 * Set max number of classes allowed. default is 15.
 	 * 
 	 * @param i
 	 *            max number of cls allowed.
 	 */
 	public void setMaxCls(int i) {
 		maxCls = i;
 		System.out.println("max number of cls set to:" + i);
 	}
 
 	/**
 	 * Get max number of classes allowed.
 	 * 
 	 * @return number of max classes.
 	 */
 	public int getMaxCls() {
 		return maxCls;
 	}
 
 	public int getMaxStdPerCls() {
 		return maxStdPerCls;
 	}
 
 	/**
 	 * Move a student from one class to another.
 	 * 
 	 * @param fromCls
 	 * @param toCls
 	 * @param std
 	 * @throws StdClsCompatibleException
 	 */
 	public void moveStd(Classes fromCls, Classes toCls, Students std) {
 		// throws StdClsCompatibleException {
 		// throwing these exceptions defeats the purpose of manual modification
 		// if (!fromCls.getClsName().equals(toCls.getClsName())) {
 		// throw new StdClsCompatibleException(0);
 		// } else if (toCls.getTotal() == 5) {
 		// throw new StdClsCompatibleException(1);
 		// } else if (Math.abs(fromCls.getLowestAge() - toCls.getLowestAge()) >
 		// 3.92) {
 		// throw new StdClsCompatibleException(2);
 		// } else if (!BLfit(std, toCls)) {
 		// throw new StdClsCompatibleException(3);
 		// } else if (!compatible(std, toCls)) {
 		// throw new StdClsCompatibleException(4);
 		// } else {
 		toCls.addStd(std);
 		fromCls.removeStd(std.getId());
 
 		// console output
 		System.out.println(toCls.getFormalClassName() + " added student:"
 				+ std.toString() + ":");
 		System.out.println(toCls.toString());
 		System.out.println(fromCls.getFormalClassName() + " deleted student:"
 				+ std.toString() + ":");
 		System.out.println(fromCls.toString());
 
 		//find a fit std from waitlist
 		for (Students stdt : unlucky) {
 			Classes math = null, la = null, read = null, hr = null, sp = null;
 			for (Classes cls : this.mathClsLst) {
 				if (this.compatible(stdt, cls)) {
 					math = cls;
 				}
 			}
 			for (Classes cls : this.laClsLst) {
 				if (this.compatible(stdt, cls)) {
 					la = cls;
 				}
 			}
 			for (Classes cls : this.readClsLst) {
 				if (this.compatible(stdt, cls)) {
 					read = cls;
 				}
 			}
 			for (Classes cls : this.homeroomClsLst) {
 				if (this.compatible(stdt, cls)) {
 					hr = cls;
 				}
 			}
 			for (Classes cls : this.specialClsLst) {
 				if (this.compatible(stdt, cls)) {
 					sp = cls;
 				}
 			}
 			if (math != null && la != null && read != null && sp != null
 					&& hr != null) {
 				unlucky.remove(stdt);
 				stdt.setWlReason("");
 				math.addStd(stdt);
 				la.addStd(stdt);
 				read.addStd(stdt);
 				hr.addStd(stdt);
 				sp.addStd(stdt);
 
 				System.out.println("From waitlist,student:" + stdt.toString()
 						+ " has been added to following classes: ");
				System.out.println(math.toString());
				System.out.println(la.toString());
				System.out.println(read.toString());
				System.out.println(hr.toString());
				System.out.println(sp.toString());
 
 			}
 
 			// if (compatible(stdt, fromCls)) { //TODO fixit: I forget to check
 			// other 4 classes compatibility
 			// fromCls.addStd(stdt);
 			// unlucky.remove(stdt);
 			//
 			// System.out.println("From waitlist,student:" + stdt.toString()
 			// + " has been added to class "
 			// + fromCls.getFormalClassName());
 			// break;
 			// }
 		}
 	}
 
 	public int getMostClasses() {
 		return Math.max(getTotalRead(), Math.max(getTotalMath(), getTotalLA()));
 	}
 
 	/**
 	 * Even distribute any 2 classes with movable students so that we won't have
 	 * one class of 5 std while one in another.
 	 */
 	public void evenDistribute() {
 		// TODO
 	}
 
 	/**
 	 * Totally remove a student from system.
 	 * 
 	 * @param id
 	 *            student ID
 	 */
 	public void kickout(Students std) {
 		if (unlucky.contains(std)) {
 			unlucky.remove(std);
 		} else {
 			// kick out
 			std.getMathCls().removeStd(std.getId());
 			std.getLACls().removeStd(std.getId());
 			std.getReadCls().removeStd(std.getId());
 			std.getHomeroomCls().removeStd(std.getId());
 			std.getSpecialCls().removeStd(std.getId());
 
 			// find fit from waitlist
 			for (Students stdt : unlucky) {
 				if (compatible(stdt, std.getMathCls())
 						&& compatible(stdt, std.getLACls())
 						&& compatible(stdt, std.getReadCls())
 						&& compatible(stdt, std.getHomeroomCls())
 						&& compatible(stdt, std.getSpecialCls())) {
 					std.getMathCls().addStd(stdt);
 					std.getLACls().addStd(stdt);
 					std.getReadCls().addStd(stdt);
 					std.getHomeroomCls().addStd(stdt);
 					std.getSpecialCls().addStd(stdt);
 
 					unlucky.remove(stdt);
 					stdt.setWlReason("");
 
 				}
 
 			}
 		}
 	}
 
 	// public static Classes[] getStdClasses(Students std) {
 	// Classes[] classes = new Classes[3];
 	// //classes[0] - math / classes[1] - la / classes[2] - read
 	// for (Classes cls:mathClsLst) {
 	// if (cls.hasStudent(std)) {
 	// classes[0] = cls;
 	// }
 	// }
 	// for (Classes cls:laClsLst) {
 	// if (cls.hasStudent(std)) {
 	// classes[1] = cls;
 	// }
 	// }
 	// for (Classes cls:readClsLst) {
 	// if (cls.hasStudent(std)) {
 	// classes[2] = cls;
 	// }
 	// }
 	// return classes;
 	// }
 
 }
