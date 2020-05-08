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
 	private MainFrame mf;
 
 	public final String noFitMath = "There are no suitable math classes for this student.";
 	public final String noFitLA = "There are no suitable language art classes for this student.";
 	public final String noFitRead = "There are no suitable reading classes for this student.";
 
 	List<Classes> mathClsLst;
 	List<Classes> laClsLst;
 	List<Classes> readClsLst;
 	List<Classes> homeroomClsLst;
 	List<Classes> specialClsLst;
 	List<Students> unlucky;
 
 	public ClassFactory(MainFrame m) {
 		mf = m;
 		mathClsLst = new ArrayList<Classes>();
 		laClsLst = new ArrayList<Classes>();
 		readClsLst = new ArrayList<Classes>();
 		homeroomClsLst = new ArrayList<Classes>();
 		specialClsLst = new ArrayList<Classes>();
 		unlucky = new ArrayList<Students>();
 	}
 	
 	public void reinit (MainFrame m) {
 		mf = m;
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
 		if (cls.getTotal() == 0) {
 
 			return true;
 		}
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
 		System.out.println("max number of cls set to: " + i);
 		if (mf.sched != null)
 			mf.sched.update();
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
 		if (fromCls != null) {
 			fromCls.removeStd(std.getId());
 		} else {
 			unlucky.remove(std);
 			std.setWlReason("");
 		}
 
 		// console output
 		System.out.println(toCls.getFormalClassName() + " added student:"
 				+ std.toString() + ":");
 		System.out.println(toCls.toString());
 		if (fromCls != null) {
 			System.out.println(fromCls.getFormalClassName()
 					+ " deleted student:" + std.toString() + ":");
 
 			System.out.println(fromCls.toString());
 		}
 
 		// find a fit std from waitlist
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
 				System.out.println(math.getFormalClassName());
 				System.out.println(la.getFormalClassName());
 				System.out.println(read.getFormalClassName());
 				System.out.println(hr.getFormalClassName());
 				System.out.println(sp.getFormalClassName());
 				break;
 
 			}
 			
 
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
 			// Get classes before kicking out
 			Classes m = std.getMathCls();
 			Classes l = std.getLACls();
 			Classes r = std.getReadCls();
 			Classes h = std.getHomeroomCls();
 			Classes s = std.getSpecialCls();
 
 			// kick out
 			std.getMathCls().removeStd(std.getId());
 			std.getLACls().removeStd(std.getId());
 			std.getReadCls().removeStd(std.getId());
 			std.getHomeroomCls().removeStd(std.getId());
 			std.getSpecialCls().removeStd(std.getId());
 
 			// find fit from waitlist
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
 					System.out.println(math.getFormalClassName());
 					System.out.println(la.getFormalClassName());
 					System.out.println(read.getFormalClassName());
 					System.out.println(hr.getFormalClassName());
 					System.out.println(sp.getFormalClassName());
 					break;
 
 				}
 //			for (Students stdt : unlucky) {
 //				if (compatible(stdt, m) && compatible(stdt, l)
 //						&& compatible(stdt, r) && compatible(stdt, h)
 //						&& compatible(stdt, s)) {
 //					m.addStd(stdt);
 //					l.addStd(stdt);
 //					r.addStd(stdt);
 //					h.addStd(stdt);
 //					s.addStd(stdt);
 //
 //					unlucky.remove(stdt);
 //					stdt.setWlReason("");
 //					System.out.println("From waitlist,student:" + stdt.toString()
 //							+ " has been added to following classes: ");
 //					System.out.println(m.getFormalClassName());
 //					System.out.println(l.getFormalClassName());
 //					System.out.println(r.getFormalClassName());
 //					System.out.println(h.getFormalClassName());
 //					System.out.println(s.getFormalClassName());
 //					break;
 //
 //				}
 
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
 
 	public void resetClsID() {
 		this.clsID = 1;
 	}
 
 	/**
 	 * Append empty classes to each of 5 class list, up to max cls # allowed.
 	 */
 	public void appendEmtpyCls() {
 		int totalMath=this.getTotalMath();
 		int totalLA = this.getTotalLA();
 		int totalRead = this.getTotalRead();
 
 		for (int i = 0; i < this.getMaxCls() - totalMath; i++) {
 			this.mathClsLst.add(this.createClass("math", 0));
 			this.homeroomClsLst.add(this.createClass("homeroom", 0));
 			this.specialClsLst.add(this.createClass("special", 0));
 		}
 		for (int i = 0; i < this.getMaxCls() - totalLA; i++) {
 			this.laClsLst.add(this.createClass("la", 0));
 		}
 		for (int i = 0; i < this.getMaxCls() - totalRead; i++) {
 			this.readClsLst.add(this.createClass("read", 0));
 		}
 	}
 	
 	public void modifyCls(int ID, Classes cls) {
 		for (int i = 0; i < mathClsLst.size(); i ++) {
 			if (mathClsLst.get(i).getClsID() == ID) {
 				mathClsLst.set(i, cls);
 			}
 		}
 		
 		for (int i = 0; i < laClsLst.size(); i ++) {
 			if (laClsLst.get(i).getClsID() == ID) {
 				laClsLst.set(i, cls);
 			}
 		}
 		
 		for (int i = 0; i < readClsLst.size(); i ++) {
 			if (readClsLst.get(i).getClsID() == ID) {
 				readClsLst.set(i, cls);
 			}
 		}
 		
 		for (int i = 0; i < homeroomClsLst.size(); i ++) {
 			if (homeroomClsLst.get(i).getClsID() == ID) {
 				homeroomClsLst.set(i, cls);
 			}
 		}
 		
 		for (int i = 0; i < specialClsLst.size(); i ++) {
 			if (specialClsLst.get(i).getClsID() == ID) {
 				specialClsLst.set(i, cls);
 			}
 		}
 	}
 }
