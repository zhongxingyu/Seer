 package elw.vo;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.List;
 import java.util.Map;
 
 public class IndexEntry {
 	protected String[] path;
 	protected int scoreBudget;
 	protected int classFrom;
 	protected Map<String, Integer> classDue;
 	protected boolean requireClean;
 
 	public boolean isRequireClean() {
 		return requireClean;
 	}
 
 	public void setRequireClean(boolean requireClean) {
 		this.requireClean = requireClean;
 	}
 
 	public int getClassFrom() {
 		return classFrom;
 	}
 
 	public void setClassFrom(int classFrom) {
 		this.classFrom = classFrom;
 	}
 
 	public int getScoreBudget() {
 		return scoreBudget;
 	}
 
 	public void setScoreBudget(int scoreBudget) {
 		this.scoreBudget = scoreBudget;
 	}
 
 	public String[] getPath() {
 		return path;
 	}
 
 	public void setPath(String[] path) {
 		this.path = path;
 	}
 
 	public Map<String, Integer> getClassDue() {
 		return classDue;
 	}
 
 	public void setClassDue(Map<String, Integer> classDue) {
 		this.classDue = classDue;
 	}
 
 	public String getListStatusHtml(Format fmt, Enrollment enr, final FileSlot slot, List<Entry<FileMeta>> uploadMetas) {
 		if (uploadMetas != null && uploadMetas.size() > 0) {
 			return getStatusHtml(fmt, enr, slot, uploadMetas.get(uploadMetas.size() - 1).getMeta());
 		} else {
 			return getStatusHtml(fmt, enr, slot, null);
 		}
 	}
 
 	public String getStatusHtml(Format fmt, Enrollment enr, final FileSlot slot, FileMeta uploadMeta) {
 		Integer classDueIdx = null;
 		if (classDue != null) {
 			classDueIdx = classDue.get(slot.getId());
 		}
 		final StringBuilder result = new StringBuilder();
 
 		if (!enr.getClasses().get(classFrom).isStarted()) {
 			result.append("Closed");
 
 			result.append("; Opens ").append(fmt.format(enr.getClasses().get(classFrom).getFromDateTime().getMillis()));
 		} else {
 			if (uploadMeta == null) {
 				result.append("Open");
 			} else {
 				if (uploadMeta.getScore() == null) {
 					result.append("Pending");
 				} else {
 					if (uploadMeta.getScore().isApproved()) {
 						result.append("Approved");
 					} else {
 						result.append("Declined");
 					}
 				}
 			}
 
 			if (classDueIdx == null) {
 				result.append("; No Due Date");
 			} else {
 				final Class dueClass = enr.getClasses().get(classDueIdx);
 				result.append("; Due ").append(fmt.format(dueClass.getToDateTime().getMillis()));
 			}
 		}
 
 
 		return result.toString();
 	}
 
 	public String getListStatusClasses(Enrollment enr, final FileSlot slot, List<Entry<FileMeta>> uploadMetas) {
 		if (uploadMetas != null && uploadMetas.size() > 0) {
 			return getStatusClasses(enr, slot, uploadMetas.get(uploadMetas.size() - 1).getMeta());
 		} else {
 			return getStatusClasses(enr, slot, null);
 		}
 	}
 
 	public String getStatusClasses(Enrollment enr, final FileSlot slot, FileMeta uploadMeta) {
 		Integer classDueIdx = null;
 		if (classDue != null) {
 			classDueIdx = classDue.get(slot.getId());
 		}
 
 		final StringBuilder result = new StringBuilder();
 
 		if (!enr.getClasses().get(classFrom).isStarted()) {
 			result.append(" elw_closed");
 		} else {
 			if (uploadMeta != null) {
 				if (uploadMeta.getScore() == null) {
 					result.append(" elw_pending");
 				} else {
 					if (uploadMeta.getScore().isApproved()) {
 						result.append(" elw_approved");
 					} else {
 						result.append(" elw_declined");
 					}
 				}
 			} else {
 				result.append(" elw_open");
 			}
 		}
 
 
 		if (classDueIdx != null) {
 			final Class dueClass = enr.classes.get(classDueIdx);
 			if (uploadMeta != null && dueClass.isPassed()) {
 				result.append(" elw_due_passed");
 			}
 			if (
 				(uploadMeta != null && dueClass.getToDateTime().getMillis() < uploadMeta.getCreateStamp().getTime()) ||
 				(uploadMeta == null && dueClass.isPassed())
 			) {
 				result.append(" elw_overdue");
 			}
 		} else {
 			result.append(" elw_nodue");
 		}
 
 		return result.toString().trim();
 	}
 
 	//	TODO pass Ctx to this place somehow
 	public String normName(final Enrollment enr, final Student stud, final Assignment ass, final Version ver,
 						   final FileSlot slot, final FileMeta meta, final Format format) {
 		try {
 			final String normName = enr.getName() + "-" + stud.getName() + "--" +
 					ass.getName() + "-" + ver.getName() + "--" +
 					slot.getName() + "-" + format.format(meta.getCreateStamp().getTime(), "MMdd-HHmm");
 
 			final String oriName = meta.getName();
 			final int oriLastDot = oriName.lastIndexOf(".");
 			final String oriExt = oriLastDot < 0 ? "" : oriName.substring(oriLastDot);
 
			final String normNameNoWs = normName.replaceAll("\\s", "_") + oriExt;
 
 			return URLEncoder.encode(
 					normNameNoWs,
 					"UTF-8"
 			);
 		} catch (UnsupportedEncodingException e) {
 			throw new IllegalStateException("UTF-8 is NOT supported?!");
 		}
 	}
 }
