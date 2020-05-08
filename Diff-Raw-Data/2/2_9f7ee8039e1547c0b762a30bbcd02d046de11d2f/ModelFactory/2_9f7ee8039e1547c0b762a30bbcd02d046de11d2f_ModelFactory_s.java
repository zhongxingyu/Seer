 package melt.cs.manchester.factory;
 
 import java.awt.Dimension;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import melt.cs.manchester.logic.Assessment;
 import melt.cs.manchester.logic.Choice;
 import melt.cs.manchester.logic.Essay;
 import melt.cs.manchester.logic.FillInBlank;
 import melt.cs.manchester.logic.MultipleChoice;
 import melt.cs.manchester.logic.Question;
 import melt.cs.manchester.logic.Section;
 import melt.cs.manchester.logic.SubSection;
 
 /**
  * 
  * @author CipherHat
  * @author Ruvin for reviewing and implementing algorithms
  */
 public class ModelFactory {
 
 	private List<Assessment> assessmentList = new LinkedList<Assessment>();
 
 	public ModelFactory() {
 		// TODO Auto-generated constructor stub
 	}
 
 	// <editor-fold defaultstate="collapsed" desc="Assessment">
 	/**
 	 * 
 	 * @param name
 	 *            cannot be NULL
 	 * @param instruction
 	 *            can be NULL
 	 * @param code
 	 *            can be NULL
 	 * @param timeInterval
 	 *            can be NULL, but the input required is int[3] with
 	 *            {hour:min:secs} format. Hour <= 23 and >= 0, Hour and Minute
 	 *            <=59 and >=0
 	 * @throws Exception
 	 *             if any condition for the input is not met
 	 */
 	public void createAssessment(String name, String instruction, String code)
 			throws Exception {
 		if (name == null) {
 			throw new Exception("MDFT1: Assessment name cannot be NULL");
 		}
 		Assessment assessment = new Assessment();
 		assessmentList.add(assessment);
 		setAssessmentName(assessmentList.size(), name);
 		setAssessmentInstruction(assessmentList.size(), instruction);
 		setAssessmentCode(assessmentList.size(), code);
 	}
 
 	/**
 	 * Changing the Assessment Name on the specified position
 	 * 
 	 * @param position
 	 *            int. first index as 1
 	 * @param name
 	 *            String cannot be NULL
 	 * @throws Exception
 	 */
 	public void setAssessmentName(int position, String name) throws Exception {
 		if (!isAssessmentCreated(position - 1)) {
 			throw new Exception(
 					"MDFT2: Invalid positions provided or objects are not created yet");
 		}
 		if (name == null) {
 			throw new Exception("MDFT3: Cannot set null name to the Assessment");
 		}
 		assessmentList.get(position - 1).setName(name);
 	}
 
 	/**
 	 * Changing the Assessment instruction on the specified position
 	 * 
 	 * @param position
 	 *            int. first index as 1
 	 * @param instruction
 	 *            can be NULL
 	 * @throws Exception
 	 */
 	public void setAssessmentInstruction(int position, String instruction)
 			throws Exception {
 		if (!isAssessmentCreated(position - 1)) {
 			throw new Exception(
 					"MDFT4: Invalid positions provided or objects are not created yet");
 		}
 		assessmentList.get(position - 1).setInstruction(instruction);
 	}
 
 	/**
 	 * Changing code of the Assessment on the specified position
 	 * 
 	 * @param position
 	 *            int. first index as 1
 	 * @param code
 	 *            String
 	 * @throws Exception
 	 */
 	public void setAssessmentCode(int position, String code) throws Exception {
 		if (!isAssessmentCreated(position - 1)) {
 			throw new Exception(
 					"MDFT5: Invalid positions provided or objects are not created yet");
 		}
 		assessmentList.get(position - 1).setCode(code);
 	}
 
 	/**
 	 * 
 	 * @param position
 	 *            int. first index as 1
 	 * @return String
 	 * @throws Exception
 	 */
 	public String getAssessmentName(int position) throws Exception {
 		if (!isAssessmentCreated(position - 1)) {
 			throw new Exception(
 					"MDFT6: Invalid positions provided or objects are not created yet");
 		}
 		return assessmentList.get(position - 1).getName();
 	}
 
 	/**
 	 * Combination of Assessment Code and Name in one list of String
 	 * 
 	 * @return List<String>
 	 */
 	public List<String> getAssessmentCodeAndNameList() {
 		List<String> assessmentNameList = new ArrayList<String>();
 		for (int i = 0; i < assessmentList.size(); i++) {
 			assessmentNameList.add(assessmentList.get(i).getCode() + " - "
 					+ assessmentList.get(i).getName());
 		}
 		return assessmentNameList;
 	}
 
 	/**
 	 * 
 	 * @param position
 	 *            int. first index as 1
 	 * @return String
 	 * @throws Exception
 	 */
 	public String getAssessmentInstruction(int position) throws Exception {
 		if (!isAssessmentCreated(position - 1)) {
 			throw new Exception(
 					"MDFT7: Invalid positions provided or objects are not created yet");
 		}
 		return assessmentList.get(position - 1).getInstruction();
 	}
 
 	/**
 	 * 
 	 * @param position
 	 *            int. first index as 1
 	 * @return String
 	 * @throws Exception
 	 */
 	public String getAssessmentCode(int position) throws Exception {
 		if (!isAssessmentCreated(position - 1)) {
 			throw new Exception(
 					"MDFT8: Invalid positions provided or objects are not created yet");
 		}
 		return assessmentList.get(position - 1).getCode();
 	}
 
 	/**
 	 * @author Dommy
 	 * @param position
 	 *            required at least int[2]. first index as 1
 	 * @return int - time interval in milliseconds
 	 * @throws Exception
 	 */
 	public int getSectionTimeIntervalInSeconds(int[] positions)
 			throws Exception {
 		int assmt = positions[0] - 1;
 		int sect = positions[1] - 1;
 		if (!isAssessmentCreated(assmt)
 				|| !isSectionCreated(new int[] { assmt, sect })) {
 			throw new Exception(
 					"MDFT9: Invalid positions provided or objects are not created yet");
 		}
 		return assessmentList.get(assmt).getSectionList().get(sect)
 				.getTimeInterval();
 	}
 
 	/**
 	 * Removing assessment on the specified position
 	 * 
 	 * @param position
 	 *            int. first index as 1
 	 * @throws Exception
 	 */
 	public void removeAssessment(int position) throws Exception {
 		if (!isAssessmentCreated(position - 1)) {
 			throw new Exception(
 					"MDFT10: Invalid positions provided or objects are not created yet");
 		}
 		assessmentList.remove(position - 1);
 	}
 
 	/**
 	 * 
 	 * @author Dommy
 	 * @param position
 	 *            int. first index as 1
 	 * @return List<Section> - list of sections
 	 */
 	public List<Section> getSectionList(int position) {
 		int assmt = position - 1;
 		return assessmentList.get(assmt).getSectionList();
 	}
 
 	// </editor-fold>
 	// <editor-fold defaultstate="collapsed" desc="Checker">
 	/**
 	 * Internal Method
 	 * 
 	 * @author Ruvin time[2]!
 	 * @param time
 	 *            int[3] hh:mm:ss
 	 * @return boolean
 	 */
 	private boolean isTimeFormatValid(int[] time) {
 		if (time[0] < 0 || time[0] > 23 || time[1] < 0 || time[1] > 59
 				|| time[2] < 0 || time[2] > 59) {
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Internal method.
 	 * 
 	 * @param position
 	 *            int. first index as 0
 	 * @return boolean
 	 */
 	private boolean isAssessmentCreated(int position) {
 		if (assessmentList.get(position) == null) {
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Internal Method
 	 * 
 	 * @param positions
 	 *            int. first index as 0
 	 * @return boolean
 	 */
 	private boolean isSectionCreated(int[] positions) {
 		if (assessmentList.get(positions[0]).getSectionList().get(positions[1]) == null) {
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Internal Method
 	 * 
 	 * @param positions
 	 *            int. first index as 0
 	 * @return boolean
 	 */
 	private boolean isSubSectionCreated(int[] positions) {
 		if (assessmentList.get(positions[0]).getSectionList().get(positions[1])
 				.getSubSectionList().get(positions[2]) == null) {
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Internal Method
 	 * 
 	 * @param positions
 	 *            int.. first index as 0
 	 * @return boolean
 	 */
 	private boolean isQuestionCreated(int[] positions) {
 		if (assessmentList.get(positions[0]).getSectionList().get(positions[1])
 				.getSubSectionList().get(positions[2]).getQuestionList()
 				.get(positions[3]) == null) {
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Internal Method
 	 * 
 	 * @param positions
 	 *            int. first index as 0
 	 * @return boolean
 	 */
 	private boolean isChoicesCreated(int[] positions) {
 		MultipleChoice m = (MultipleChoice) assessmentList.get(positions[0])
 				.getSectionList().get(positions[1]).getSubSectionList()
 				.get(positions[2]).getQuestionList().get(positions[3]);
 		if (m.getChoiceList() == null) {
 			return false;
 		}
 		return true;
 	}
 
 	// </editor-fold>
 	// <editor-fold defaultstate="collapsed" desc="Section">
 	/**
 	 * Create a new Section under the Assessment
 	 * 
 	 * @param positions
 	 *            required at least int[1]. first index as 1
 	 * @param name
 	 *            String - cannot be NULL
 	 * @param instruction
 	 *            String - can be NULL
 	 * @param timeLimit
 	 *            int[3]. hh:mm:ss - cannot be NULL
 	 * @param timeInterval
 	 *            int[3]. hh:mm:ss - cannot be NULL
 	 * @throws Exception
 	 */
 	public void createSection(int[] positions, String name, String instruction,
 			int[] timeLimit, int[] timeInterval) throws Exception {
 		if (!isAssessmentCreated(positions[0] - 1)) {
 			throw new Exception(
 					"MDFT11: Invalid positions provided or objects are not created yet");
 		}
 		if (name == null) {
 			throw new Exception("MDFT47: Section name cannot be null");
 		}
 		Section section = new Section();
 		List<Section> sectionList = assessmentList.get(positions[0] - 1)
 				.getSectionList();
 		sectionList.add(section);
 		assessmentList.get(positions[0] - 1).setSectionList(sectionList);
 		int sect = sectionList.size();
 		setSectionName(new int[] { positions[0], sect }, name);
 		setSectionInstruction(new int[] { positions[0], sect }, instruction);
 		setSectionTimeLimit(new int[] { positions[0], sect }, timeLimit);
 		setSectionTimeInterval(new int[] { positions[0], sect }, timeInterval);
 	}
 
 	/**
 	 * Change section name
 	 * 
 	 * @param positions
 	 *            required at least int[2]. first index as 1
 	 * @param name
 	 *            String
 	 * @throws Exception
 	 */
 	public void setSectionName(int[] positions, String name) throws Exception {
 		int assmt = positions[0] - 1;
 		int sect = positions[1] - 1;
 
 		if (!isAssessmentCreated(assmt)
 				|| !isSectionCreated(new int[] { assmt, sect })) {
 			throw new Exception(
 					"MDFT12: Invalid positions provided or objects are not created yet");
 		}
 		if (name == null) {
 			throw new Exception("MDFT48: Name cannot be NULL");
 		}
 		assessmentList.get(assmt).getSectionList().get(sect).setName(name);
 	}
 
 	/**
 	 * Change Section instruction text
 	 * 
 	 * @param positions
 	 *            required at least int[2]. first index as 1
 	 * @param instruction
 	 *            String - can be NULL
 	 * @throws Exception
 	 */
 	public void setSectionInstruction(int[] positions, String instruction)
 			throws Exception {
 		int assmt = positions[0] - 1;
 		int sect = positions[1] - 1;
 
 		if (!isAssessmentCreated(assmt)
 				|| !isSectionCreated(new int[] { assmt, sect })) {
 			throw new Exception(
 					"MDFT13: Invalid positions provided or objects are not created yet");
 		}
 		assessmentList.get(assmt).getSectionList().get(sect)
 				.setInstruction(instruction);
 	}
 
 	/**
 	 * Change Section time limit
 	 * 
 	 * @author Ruvin
 	 * @param positions
 	 *            required at least int[2]. first index as 1
 	 * @param timeLimit
 	 *            int[3] hh:mm:ss - cannot be NULL
 	 * @throws Exception
 	 */
 	public void setSectionTimeLimit(int[] positions, int[] timeLimit)
 			throws Exception {
 		int assmt = positions[0] - 1;
 		int sect = positions[1] - 1;
 		if (!isAssessmentCreated(assmt)
 				|| !isSectionCreated(new int[] { assmt, sect })) {
 			throw new Exception(
 					"MDFT14: Invalid positions provided or objects are not created yet");
 		}
 		if (timeLimit.length == 3) {
 			if (isTimeFormatValid(timeLimit) == false) {
 				throw new Exception("MDFT15: Time format is wrong");
 			}
 		} else {
 			throw new Exception("MDFT16: Time format is wrong");
 		}
 
 		int timeConvertedInSecs = timeLimit[0] * 3600 + timeLimit[1] * 60
 				+ timeLimit[2];
 
 		assessmentList.get(assmt).getSectionList().get(sect)
 				.setTimeLimit(timeConvertedInSecs);
 	}
 
 	/**
 	 * Change Section time interval
 	 * 
 	 * @author CipherHat implement the method structure
 	 * @author Ruvin implement algorithm
 	 * @param position
 	 *            required at least int[2]. first index as 1
 	 * @param timeInterval
 	 *            int[3] hh:mm:ss - cannot be NULL
 	 * @throws Exception
 	 */
 	public void setSectionTimeInterval(int[] positions, int[] timeInterval)
 			throws Exception {
 		int assmt = positions[0] - 1;
 		int sect = positions[1] - 1;
 		if (!isAssessmentCreated(assmt)
 				|| !isSectionCreated(new int[] { assmt, sect })) {
 			throw new Exception(
 					"MDFT17: Invalid positions provided or objects are not created yet");
 		}
 
 		if (timeInterval.length == 3) {
 			if (isTimeFormatValid(timeInterval) == false) {
 				throw new Exception("MDFT18: Time format is wrong");
 			}
 		} else {
 			throw new Exception("MDFT19: Time format is wrong");
 		}
 
 		int timeConvertedInSecs = timeInterval[0] * 3600 + timeInterval[1] * 60
 				+ timeInterval[2];
 
 		assessmentList.get(assmt).getSectionList().get(sect)
 				.setTimeInterval(timeConvertedInSecs);
 	}
 
 	/**
 	 * Get Section time interval on the specified position
 	 * 
 	 * @author Ruvin
 	 * @param position
 	 *            required at least int[2]. first index as 1
 	 * @return int[3] hh:mm:ss
 	 * @throws Exception
 	 */
 	public int[] getSectionTimeInterval(int[] positions) throws Exception {
 		int assmt = positions[0] - 1;
 		int sect = positions[1] - 1;
 		if (!isAssessmentCreated(assmt)
 				|| !isSectionCreated(new int[] { assmt, sect })) {
 			throw new Exception(
 					"MDFT20: Invalid positions provided or objects are not created yet");
 		}
 
 		int[] time = new int[] { 0, 0, 0 };
 		int unconvertedSeconds = assessmentList.get(assmt).getSectionList()
 				.get(sect).getTimeInterval();
 		int hour = unconvertedSeconds / 3600;
 		unconvertedSeconds %= 3600;
 		int minute = unconvertedSeconds / 60;
 		unconvertedSeconds %= 60;
 		int second = unconvertedSeconds;
 		time[0] = hour;
 		time[1] = minute;
 		time[2] = second;
 		return time;
 	}
 
 	/**
 	 * Get section name on the specified position
 	 * 
 	 * @author Ruvin
 	 * @param positions
 	 *            required at least int[2]. first index as 1
 	 * @return String
 	 * @throws Exception
 	 */
 	public String getSectionName(int[] positions) throws Exception {
 		int assmt = positions[0] - 1;
 		int sect = positions[1] - 1;
 
 		if (!isAssessmentCreated(assmt)
 				|| !isSectionCreated(new int[] { assmt, sect })) {
 			throw new Exception(
 					"MDFT21: Invalid positions provided or objects are not created yet");
 		}
 		return assessmentList.get(assmt).getSectionList().get(sect).getName();
 	}
 
 	/**
 	 * Get section instruction on the specified position
 	 * 
 	 * @author Ruvin
 	 * @param positions
 	 *            required at least int[2]. first index as 1
 	 * @return String
 	 * @throws Exception
 	 */
 	public String getSectionInstruction(int[] positions) throws Exception {
 		int assmt = positions[0] - 1;
 		int sect = positions[1] - 1;
 		if (!isAssessmentCreated(assmt)
 				|| !isSectionCreated(new int[] { assmt, sect })) {
 			throw new Exception(
 					"MDFT22: Invalid positions provided or objects are not created yet");
 		}
 		return assessmentList.get(assmt).getSectionList().get(sect)
 				.getInstruction();
 	}
 
 	/**
 	 * Get section time limit on the specified position
 	 * 
 	 * @author Ruvin
 	 * @param positions
 	 *            required at least int[2]. first index as 1
 	 * @return int[3] hh:mm:ss
 	 * @throws Exception
 	 */
 	public int[] getSectionTimeLimit(int[] positions) throws Exception {
 		int assmt = positions[0] - 1;
 		int sect = positions[1] - 1;
 		if (!isAssessmentCreated(assmt)
 				|| !isSectionCreated(new int[] { assmt, sect })) {
 			throw new Exception(
 					"MDFT23: Invalid positions provided or objects are not created yet");
 		}
 		int[] time = new int[] { 0, 0, 0 };
 		int unconvertedSeconds = assessmentList.get(assmt).getSectionList()
 				.get(sect).getTimeLimit();
 		int hour = unconvertedSeconds / 3600;
 		unconvertedSeconds %= 3600;
 		int minute = unconvertedSeconds / 60;
 		unconvertedSeconds %= 60;
 		int second = unconvertedSeconds;
 		time[0] = hour;
 		time[1] = minute;
 		time[2] = second;
 		return time;
 	}
 
 	/**
 	 * Get section time limit in seconds format
 	 * 
 	 * @author Dommy
 	 * @param positions
 	 *            required at least int[2]. first index as 1
 	 * @return int - time limit in seconds
 	 * @throws Exception
 	 */
 	public int getSectionTimeLimitInSeconds(int[] positions) throws Exception {
 		int assmt = positions[0] - 1;
 		int sect = positions[1] - 1;
 		if (!isAssessmentCreated(assmt)
 				|| !isSectionCreated(new int[] { assmt, sect })) {
 			throw new Exception(
 					"MDFT24: Invalid positions provided or objects are not created yet");
 		}
 		int timeLimit = assessmentList.get(assmt).getSectionList().get(sect)
 				.getTimeLimit();
 		assessmentList.get(assmt).getSectionList().get(sect).setTimeLimit(0);
 		return timeLimit;
 	}
 
 	/**
 	 * Removing section on the specified position
 	 * 
 	 * @param positions
 	 *            required at least int[2]. first index as 1
 	 * @throws Exception
 	 */
 	public void removeSection(int[] positions) throws Exception {
 		int assmt = positions[0] - 1;
 		int sect = positions[1] - 1;
 		if (!isAssessmentCreated(assmt)
 				|| !isSectionCreated(new int[] { assmt, sect })) {
 			throw new Exception(
 					"MDFT25: Invalid positions provided or objects are not created yet");
 		}
 		assessmentList.get(assmt).getSectionList().remove(sect);
 	}
 
 	/**
 	 * Get list of subsection
 	 * 
 	 * @author Dommy
 	 * @param positions
 	 *            required at least int[2]. first index as 1
 	 * @return List<SubSection> list of subsections
 	 */
 	public List<SubSection> getSubSectionList(int[] positions) {
 		int assmt = positions[0] - 1;
 		int sect = positions[1] - 1;
 		return assessmentList.get(assmt).getSectionList().get(sect)
 				.getSubSectionList();
 	}
 
 	// </editor-fold>
 	// <editor-fold defaultstate="collapsed" desc="SubSection">
 
 	/**
 	 * Create a new SubSection under the Section or under another SubSection
 	 * based on the specified position
 	 * 
 	 * @param positions
 	 *            required at least int[2]. first index as 1
 	 * @param name
 	 *            String - cannot be NULL
 	 * @param instruction
 	 *            String can be NULL
 	 * @throws Exception
 	 */
 	public void createSubSection(int[] positions, String name,
 			String instruction) throws Exception {
 		int[] positionsTemp = new int[2];
 		positionsTemp[0] = positions[0] - 1;
 		positionsTemp[1] = positions[1] - 1;
 		if (!isAssessmentCreated(positionsTemp[0])
 				|| !isSectionCreated(positionsTemp)) {
 			throw new Exception(
 					"MDFT26: Invalid positions provided or objects are not created yet");
 		}
 		if (name == null) {
 			throw new Exception("MDFT49: Name cannot be null");
 		}
 		SubSection subSection = new SubSection();
 		List<SubSection> subSectionList = assessmentList.get(positionsTemp[0])
 				.getSectionList().get(positionsTemp[1]).getSubSectionList();
 		subSectionList.add(subSection);
 		int ss = subSectionList.size();
 		assessmentList.get(positionsTemp[0]).getSectionList()
 				.get(positionsTemp[1]).setSubSectionList(subSectionList);
 		setSubSectionName(new int[] { positions[0], positions[1], ss }, name);
 		setSubSectionInstruction(new int[] { positions[0], positions[1], ss },
 				instruction);
 	}
 
 	/**
 	 * Change SubSection name on the specified position
 	 * 
 	 * @param positions
 	 *            required at least int[3]. first index as 1
 	 * @param name
 	 *            String - cannot be NULL
 	 * @throws Exception
 	 */
 	public void setSubSectionName(int[] positions, String name)
 			throws Exception {
 		int assmt = positions[0] - 1;
 		int sect = positions[1] - 1;
 		int subs = positions[2] - 1;
 		if (!isAssessmentCreated(assmt)
 				|| !isSectionCreated(new int[] { assmt, sect, subs })
 				|| !isSubSectionCreated(new int[] { assmt, sect, subs })) {
 			throw new Exception(
 					"MDFT27: Invalid positions provided or objects are not created yet");
 		}
 		if (name == null) {
 			throw new Exception("MDFT50: Name cannot be NULL");
 		}
 		assessmentList.get(assmt).getSectionList().get(sect)
 				.getSubSectionList().get(subs).setName(name);
 	}
 
 	/**
 	 * Change SubSection instruction on the specified position
 	 * 
 	 * @param positions
 	 *            required at least int[3]. first index as 1
 	 * @param instruction
 	 *            String
 	 * @throws Exception
 	 */
 	public void setSubSectionInstruction(int[] positions, String instruction)
 			throws Exception {
 		int assmt = positions[0] - 1;
 		int sect = positions[1] - 1;
 		int subs = positions[2] - 1;
 		if (!isAssessmentCreated(assmt)
 				|| !isSectionCreated(new int[] { assmt, sect, subs })
 				|| !isSubSectionCreated(new int[] { assmt, sect, subs })) {
 			throw new Exception(
 					"MDFT28: Invalid positions provided or objects are not created yet");
 		}
 
 		assessmentList.get(assmt).getSectionList().get(sect)
 				.getSubSectionList().get(subs).setInstruction(instruction);
 	}
 
 	/**
 	 * @author Ruvin
 	 * @param positions
 	 * @return
 	 * @throws Exception
 	 */
 	public String getSubSectionName(int[] positions) throws Exception {
 		int assmt = positions[0] - 1;
 		int sect = positions[1] - 1;
 		int subs = positions[2] - 1;
 		if (!isAssessmentCreated(assmt)
 				|| !isSectionCreated(new int[] { assmt, sect, subs })
 				|| !isSubSectionCreated(new int[] { assmt, sect, subs })) {
 			throw new Exception(
 					"MDFT29: Invalid positions provided or objects are not created yet");
 		}
 		return assessmentList.get(assmt).getSectionList().get(sect)
 				.getSubSectionList().get(subs).getName();
 	}
 
 	/**
 	 * @author Ruvin
 	 * @param positions
 	 * @return
 	 * @throws Exception
 	 */
 	public String getSubSectionInstruction(int[] positions) throws Exception {
 		int assmt = positions[0] - 1;
 		int sect = positions[1] - 1;
 		int subs = positions[2] - 1;
 		if (!isAssessmentCreated(assmt)
 				|| !isSectionCreated(new int[] { assmt, sect, subs })
 				|| !isSubSectionCreated(new int[] { assmt, sect, subs })) {
 			throw new Exception(
 					"MDFT30: Invalid positions provided or objects are not created yet");
 		}
 		return assessmentList.get(assmt).getSectionList().get(sect)
 				.getSubSectionList().get(subs).getInstruction();
 	}
 
 	public void removeSubSection(int[] positions) throws Exception {
 		int assmt = positions[0] - 1;
 		int sect = positions[1] - 1;
 		int subs = positions[2] - 1;
 		if (!isAssessmentCreated(assmt)
 				|| !isSectionCreated(new int[] { assmt, sect, subs })
 				|| !isSubSectionCreated(new int[] { assmt, sect, subs })) {
 			throw new Exception(
 					"MDFT31: Invalid positions provided or objects are not created yet");
 		}
 		assessmentList.get(assmt).getSectionList().get(sect)
 				.getSubSectionList().remove(subs);
 	}
 
 	// </editor-fold>
 	// <editor-fold defaultstate="collapsed" desc="Question">
 
 	/**
 	 * 
 	 * @param positions
 	 *            need to provide AT LEAST array of positions for {assessment,
 	 *            section, subsection}.
 	 * @param questionText
 	 *            question text for Multiple Choice Question
 	 * @param mark
 	 *            marks for this question
 	 * @throws Exception
 	 *             if position is invalid
 	 */
 	public void createMultipleChoiceQuestion(int[] positions,
 			String questionText, double mark) throws Exception {
 		int[] positionsTemp = new int[3];
 		positionsTemp[0] = positions[0] - 1;
 		positionsTemp[1] = positions[1] - 1;
 		positionsTemp[2] = positions[2] - 1;
 		if (!isAssessmentCreated(positionsTemp[0])
 				|| !isSectionCreated(positionsTemp)
 				|| !isSubSectionCreated(positionsTemp)) {
 			throw new Exception(
 					"MDFT32: Invalid positions provided or objects are not created yet");
 		}
 		Question question = new MultipleChoice();
 		assessmentList.get(positionsTemp[0]).getSectionList()
 				.get(positionsTemp[1]).getSubSectionList()
 				.get(positionsTemp[2]).getQuestionList().add(question);
 		int q = assessmentList.get(positionsTemp[0]).getSectionList()
 				.get(positionsTemp[1]).getSubSectionList()
 				.get(positionsTemp[2]).getQuestionList().size();
 		setQuestionText(
 				new int[] { positions[0], positions[1], positions[2], q },
 				questionText);
 		setQuestionMark(
 				new int[] { positions[0], positions[1], positions[2], q }, mark);
 	}
 
 	public void createMultipleChoiceQuestion(int[] positions,
 			String questionText, double mark, List<String> textList,
 			List<Boolean> trueList) throws Exception {
 		int[] positionsTemp = new int[3];
 		positionsTemp[0] = positions[0] - 1;
 		positionsTemp[1] = positions[1] - 1;
 		positionsTemp[2] = positions[2] - 1;
 		if (!isAssessmentCreated(positionsTemp[0])
 				|| !isSectionCreated(positionsTemp)
 				|| !isSubSectionCreated(positionsTemp)) {
 			throw new Exception(
 					"MDFT33: Invalid positions provided or objects are not created yet");
 		}
 		Question question = new MultipleChoice();
 		assessmentList.get(positionsTemp[0]).getSectionList()
 				.get(positionsTemp[1]).getSubSectionList()
 				.get(positionsTemp[2]).getQuestionList().add(question);
 		int q = assessmentList.get(positionsTemp[0]).getSectionList()
 				.get(positionsTemp[1]).getSubSectionList()
 				.get(positionsTemp[2]).getQuestionList().size();
 		setQuestionText(
 				new int[] { positions[0], positions[1], positions[2], q },
 				questionText);
 		setQuestionMark(
 				new int[] { positions[0], positions[1], positions[2], q }, mark);
 		for (int i = 0; i < textList.size(); i++) {
 			addChoices(
 					new int[] { positions[0], positions[1], positions[2], q },
 					textList.get(i), trueList.get(i));
 		}
 	}
 
 	public void addChoices(int[] positions, String choiceText,
 			boolean trueChoice) throws Exception {
 		int assmt = positions[0] - 1;
 		int sect = positions[1] - 1;
 		int subs = positions[2] - 1;
 		int quest = positions[3] - 1;
 
 		if (!isAssessmentCreated(assmt)
 				|| !isSectionCreated(new int[] { assmt, sect, subs, quest })
 				|| !isSubSectionCreated(new int[] { assmt, sect, subs, quest })
 				|| !isQuestionCreated(new int[] { assmt, sect, subs, quest })
 				|| !(assessmentList.get(assmt).getSectionList().get(sect)
 						.getSubSectionList().get(subs).getQuestionList()
 						.get(quest) instanceof MultipleChoice)) {
 			throw new Exception(
 					"MDFT34: Invalid positions provided or objects are not created yet");
 		}
 
 		Choice choice = new Choice();
 		choice.setChoiceText(choiceText);
 		choice.setIsTrue(trueChoice);
 		((MultipleChoice) assessmentList.get(assmt).getSectionList().get(sect)
 				.getSubSectionList().get(subs).getQuestionList().get(quest))
 				.getChoiceList().add(choice);
 	}
 
 	public List<String> getChoiceTextList(int[] positions) {
 		int assmt = positions[0] - 1;
 		int sect = positions[1] - 1;
 		int subs = positions[2] - 1;
 		int quest = positions[3] - 1;
 		List<String> choiceTextList = new ArrayList<String>();
 		MultipleChoice m = (MultipleChoice) assessmentList.get(assmt)
 				.getSectionList().get(sect).getSubSectionList().get(subs)
 				.getQuestionList().get(quest);
 		for (int i = 0; i < m.getChoiceList().size(); i++) {
 			choiceTextList.add(m.getChoiceList().get(i).getChoiceText());
 		}
 		return choiceTextList;
 	}
 
 	public List<Boolean> getChoiceBooleanList(int[] positions) {
 		int assmt = positions[0] - 1;
 		int sect = positions[1] - 1;
 		int subs = positions[2] - 1;
 		int quest = positions[3] - 1;
 		List<Boolean> choiceBooleanList = new ArrayList<Boolean>();
 		MultipleChoice m = (MultipleChoice) assessmentList.get(assmt)
 				.getSectionList().get(sect).getSubSectionList().get(subs)
 				.getQuestionList().get(quest);
 		for (int i = 0; i < m.getChoiceList().size(); i++) {
 			choiceBooleanList.add(m.getChoiceList().get(i).isIsTrue());
 		}
 		return choiceBooleanList;
 	}
 
 	public void removeAllChoiceList(int[] positions) {
 		int assmt = positions[0] - 1;
 		int sect = positions[1] - 1;
 		int subs = positions[2] - 1;
 		int quest = positions[3] - 1;
 		MultipleChoice m = (MultipleChoice) assessmentList.get(assmt)
 				.getSectionList().get(sect).getSubSectionList().get(subs)
 				.getQuestionList().get(quest);
 		m.getChoiceList().removeAll(m.getChoiceList());
 		assessmentList.get(assmt).getSectionList().get(sect)
 				.getSubSectionList().get(subs).getQuestionList().remove(quest);
 		assessmentList.get(assmt).getSectionList().get(sect)
 				.getSubSectionList().get(subs).getQuestionList().add(quest, m);
 	}
 
 	public void setQuestionText(int[] positions, String questionText)
 			throws Exception {
 		int assmt = positions[0] - 1;
 		int sect = positions[1] - 1;
 		int subs = positions[2] - 1;
 		int quest = positions[3] - 1;
 		if (!isAssessmentCreated(assmt)
 				|| !isSectionCreated(new int[] { assmt, sect, subs, quest })
 				|| !isSubSectionCreated(new int[] { assmt, sect, subs, quest })
 				|| !isQuestionCreated(new int[] { assmt, sect, subs, quest })) {
 			throw new Exception(
 					"MDFT35: Invalid positions provided or objects are not created yet");
 		}
 
 		assessmentList.get(assmt).getSectionList().get(sect)
 				.getSubSectionList().get(subs).getQuestionList().get(quest)
 				.setQuestionOrInstruction(questionText);
 	}
 
 	public void setQuestionMark(int[] positions, double mark) throws Exception {
 		int assmt = positions[0] - 1;
 		int sect = positions[1] - 1;
 		int subs = positions[2] - 1;
 		int quest = positions[3] - 1;
 		if (!isAssessmentCreated(assmt)
 				|| !isSectionCreated(new int[] { assmt, sect, subs, quest })
 				|| !isSubSectionCreated(new int[] { assmt, sect, subs, quest })
 				|| !isQuestionCreated(new int[] { assmt, sect, subs, quest })) {
 			throw new Exception(
 					"MDFT36: Invalid positions provided or objects are not created yet");
 		}
 
 		assessmentList.get(assmt).getSectionList().get(sect)
 				.getSubSectionList().get(subs).getQuestionList().get(quest)
 				.setMark(mark);
 	}
 
 	/**
 	 * @author Ruvin
 	 * @param positions
 	 * @return
 	 * @throws Exception
 	 */
 	public String getQuestionText(int[] positions) throws Exception {
 		int assmt = positions[0] - 1;
 		int sect = positions[1] - 1;
 		int subs = positions[2] - 1;
 		int quest = positions[3] - 1;
 		if (!isAssessmentCreated(assmt)
 				|| !isSectionCreated(new int[] { assmt, sect, subs, quest })
 				|| !isSubSectionCreated(new int[] { assmt, sect, subs, quest })
 				|| !isQuestionCreated(new int[] { assmt, sect, subs, quest })) {
 			throw new Exception(
 					"MDFT37: Invalid positions provided or objects are not created yet");
 		}
 
 		return assessmentList.get(assmt).getSectionList().get(sect)
 				.getSubSectionList().get(subs).getQuestionList().get(quest)
 				.getQuestionOrInstruction();
 	}
 
 	/**
 	 * @author Ruvin
 	 * @param positions
 	 * @return
 	 * @throws Exception
 	 */
 	public double getQuestionMark(int[] positions) throws Exception {
 		int assmt = positions[0] - 1;
 		int sect = positions[1] - 1;
 		int subs = positions[2] - 1;
 		int quest = positions[3] - 1;
 		if (!isAssessmentCreated(assmt)
 				|| !isSectionCreated(new int[] { assmt, sect, subs, quest })
 				|| !isSubSectionCreated(new int[] { assmt, sect, subs, quest })
 				|| !isQuestionCreated(new int[] { assmt, sect, subs, quest })) {
 			throw new Exception(
 					"MDFT38: Invalid positions provided or objects are not created yet");
 		}
 
 		return assessmentList.get(assmt).getSectionList().get(sect)
 				.getSubSectionList().get(subs).getQuestionList().get(quest)
 				.getMark();
 	}
 
 	public void createFillInBlankQuestion(int[] positions,
 			String instructionText, double mark) throws Exception {
 		int[] positionsTemp = new int[3];
 		positionsTemp[0] = positions[0] - 1;
 		positionsTemp[1] = positions[1] - 1;
 		positionsTemp[2] = positions[2] - 1;
 		if (!isAssessmentCreated(positionsTemp[0])
 				|| !isSectionCreated(positionsTemp)
 				|| !isSubSectionCreated(positionsTemp)) {
 			throw new Exception(
 					"MDFT39: Invalid positions provided or objects are not created yet");
 		}
 		Question question = new FillInBlank();
 		List<Question> questionList = assessmentList.get(positionsTemp[0])
 				.getSectionList().get(positionsTemp[1]).getSubSectionList()
 				.get(positionsTemp[2]).getQuestionList();
 		questionList.add(question);
 		int q = questionList.size();
 		assessmentList.get(positionsTemp[0]).getSectionList()
 				.get(positionsTemp[1]).getSubSectionList()
 				.get(positionsTemp[2]).setQuestionList(questionList);
 		setQuestionText(
 				new int[] { positions[0], positions[1], positions[2], q },
 				instructionText);
 		setQuestionMark(
 				new int[] { positions[0], positions[1], positions[2], q }, mark);
 	}
 
 	/**
 	 * Pretty self describing. Removing question at specified position
 	 * 
 	 * @param positions
 	 *            required at least int[4]
 	 * @throws Exception
 	 */
 	public void removeQuestion(int[] positions) throws Exception {
 		int assmt = positions[0] - 1;
 		int sect = positions[1] - 1;
 		int subs = positions[2] - 1;
 		int quest = positions[3] - 1;
 		if (!isAssessmentCreated(assmt)
 				|| !isSectionCreated(new int[] { assmt, sect, subs, quest })
 				|| !isSubSectionCreated(new int[] { assmt, sect, subs, quest })
 				|| !isQuestionCreated(new int[] { assmt, sect, subs, quest })) {
 			throw new Exception(
 					"MDFT40: Invalid positions provided or objects are not created yet");
 		}
 		assessmentList.get(assmt).getSectionList().get(sect)
 				.getSubSectionList().get(subs).getQuestionList().remove(quest);
 	}
 
 	/**
 	 * To know what type of question to expect on each specified position
 	 * 
 	 * @param positions
 	 *            required at least int[4]
 	 * @return
 	 */
 	public String getQuestionType(int[] positions) {
 		int assmt = positions[0] - 1;
 		int sect = positions[1] - 1;
 		int subs = positions[2] - 1;
 		int quest = positions[3] - 1;
 		if (assessmentList.get(assmt).getSectionList().get(sect)
 				.getSubSectionList().get(subs).getQuestionList().get(quest) instanceof MultipleChoice) {
 			return "MCQ";
 		} else if (assessmentList.get(assmt).getSectionList().get(sect)
 				.getSubSectionList().get(subs).getQuestionList().get(quest) instanceof FillInBlank) {
 			return "FIBQ";
 		}
 		return null;
 	}
 
 	/**
 	 * Get the Question List
 	 * 
 	 * @param positions
 	 *            required at least int[3]
 	 * @return
 	 */
 	public List<Question> getQuestionList(int[] positions) {
 		int assmt = positions[0] - 1;
 		int sect = positions[1] - 1;
 		int subs = positions[2] - 1;
 		return assessmentList.get(assmt).getSectionList().get(sect)
 				.getSubSectionList().get(subs).getQuestionList();
 	}
 
 	/**
 	 * Create Essay Question with answer text and characters allowed provided
 	 * 
 	 * @param positions
 	 *            required at least int[3]
 	 * @param instructionText
 	 *            String
 	 * @param mark
 	 *            int - Full mark for the perfect essay answer
 	 * @param answerSchema
 	 *            String - the Correct answer. to be used as reference when
 	 *            marking
 	 * @param charactersAllowed
 	 *            int
 	 * @throws Exception
 	 */
 	public void createEssayQuestion(int[] positions, String instructionText,
 			double mark, String answerSchema, int charactersAllowed,
 			Dimension textAreaDimension) throws Exception {
 		int[] positionsTemp = new int[3];
 		positionsTemp[0] = positions[0] - 1;
 		positionsTemp[1] = positions[1] - 1;
 		positionsTemp[2] = positions[2] - 1;
 		if (!isAssessmentCreated(positionsTemp[0])
 				|| !isSectionCreated(positionsTemp)
 				|| !isSubSectionCreated(positionsTemp)) {
 			throw new Exception(
 					"MDFT41: Invalid positions provided or objects are not created yet");
 		}
 		Question question = new Essay();
 		List<Question> questionList = assessmentList.get(positionsTemp[0])
 				.getSectionList().get(positionsTemp[1]).getSubSectionList()
 				.get(positionsTemp[2]).getQuestionList();
 		questionList.add(question);
 		int q = questionList.size();
 		assessmentList.get(positionsTemp[0]).getSectionList()
 				.get(positionsTemp[1]).getSubSectionList()
 				.get(positionsTemp[2]).setQuestionList(questionList);
 		setQuestionText(
 				new int[] { positions[0], positions[1], positions[2], q },
 				instructionText);
 		setQuestionMark(
 				new int[] { positions[0], positions[1], positions[2], q }, mark);
 		setEssayAnswerSchema(new int[] { positions[0], positions[1],
 				positions[2], q }, answerSchema);
 		setEssayCharactersAllowed(new int[] { positions[0], positions[1],
 				positions[2], q }, charactersAllowed);
 		setEssayTextAreaDimension(new int[] { positions[0], positions[1],
 				positions[2], q }, textAreaDimension);
 	}
 
 	/**
 	 * Pretty self describing. Use this method once created the essay using
 	 * createEssayQuestion()
 	 * 
 	 * @param positions
 	 *            required at least int[4]
 	 * @param answerText
 	 *            String
 	 * @throws Exception
 	 */
 	public void setEssayAnswerSchema(int[] positions, String answerText)
 			throws Exception {
 		int assmt = positions[0] - 1;
 		int sect = positions[1] - 1;
 		int subs = positions[2] - 1;
 		int quest = positions[3] - 1;
 		if (!isAssessmentCreated(assmt)
 				|| !isSectionCreated(new int[] { assmt, sect, subs, quest })
 				|| !isSubSectionCreated(new int[] { assmt, sect, subs, quest })
 				|| !isQuestionCreated(new int[] { assmt, sect, subs, quest })
 				|| !(assessmentList.get(assmt).getSectionList().get(sect)
 						.getSubSectionList().get(subs).getQuestionList()
 						.get(quest) instanceof Essay)) {
 			throw new Exception(
 					"MDFT43: Invalid positions provided or objects are not created yet");
 		}
 		((Essay) assessmentList.get(assmt).getSectionList().get(sect)
 				.getSubSectionList().get(subs).getQuestionList().get(quest))
 				.setAnswerSchema(answerText);
 	}
 
 	/**
 	 * 
 	 * @param positions
 	 *            required at least int[4]
 	 * @return String
 	 * @throws Exception
 	 */
 	public String getEssayAnswerSchema(int[] positions) throws Exception {
 		int assmt = positions[0] - 1;
 		int sect = positions[1] - 1;
 		int subs = positions[2] - 1;
 		int quest = positions[3] - 1;
 		if (!isAssessmentCreated(assmt)
 				|| !isSectionCreated(new int[] { assmt, sect, subs, quest })
 				|| !isSubSectionCreated(new int[] { assmt, sect, subs, quest })
 				|| !isQuestionCreated(new int[] { assmt, sect, subs, quest })
 				|| !(assessmentList.get(assmt).getSectionList().get(sect)
 						.getSubSectionList().get(subs).getQuestionList()
 						.get(quest) instanceof Essay)) {
 			throw new Exception(
 					"MDFT44: Invalid positions provided or objects are not created yet");
 		}
 		return ((Essay) assessmentList.get(assmt).getSectionList().get(sect)
 				.getSubSectionList().get(subs).getQuestionList().get(quest))
 				.getAnswerSchema();
 	}
 
 	/**
 	 * Pretty self describing. Use this method once created the essay using
 	 * createEssayQuestion()
 	 * 
 	 * @param positions
 	 *            required at least int[4]
 	 * @param charactersAllowed
 	 *            int
 	 * @throws Exception
 	 */
 	public void setEssayCharactersAllowed(int[] positions, int charactersAllowed)
 			throws Exception {
 		int assmt = positions[0] - 1;
 		int sect = positions[1] - 1;
 		int subs = positions[2] - 1;
 		int quest = positions[3] - 1;
 		if (!isAssessmentCreated(assmt)
 				|| !isSectionCreated(new int[] { assmt, sect, subs, quest })
 				|| !isSubSectionCreated(new int[] { assmt, sect, subs, quest })
 				|| !isQuestionCreated(new int[] { assmt, sect, subs, quest })
 				|| !(assessmentList.get(assmt).getSectionList().get(sect)
 						.getSubSectionList().get(subs).getQuestionList()
 						.get(quest) instanceof Essay)) {
 			throw new Exception(
 					"MDFT45: Invalid positions provided or objects are not created yet");
 		}
 		((Essay) assessmentList.get(assmt).getSectionList().get(sect)
 				.getSubSectionList().get(subs).getQuestionList().get(quest))
 				.setCharactersAllowed(charactersAllowed);
 	}
 
 	/**
 	 * 
 	 * @param positions
 	 *            required at least int[4]
 	 * @return int
 	 * @throws Exception
 	 */
 	public int getEssayCharactersAllowed(int positions[]) throws Exception {
 		int assmt = positions[0] - 1;
 		int sect = positions[1] - 1;
 		int subs = positions[2] - 1;
 		int quest = positions[3] - 1;
 		if (!isAssessmentCreated(assmt)
 				|| !isSectionCreated(new int[] { assmt, sect, subs, quest })
 				|| !isSubSectionCreated(new int[] { assmt, sect, subs, quest })
 				|| !isQuestionCreated(new int[] { assmt, sect, subs, quest })
 				|| !(assessmentList.get(assmt).getSectionList().get(sect)
 						.getSubSectionList().get(subs).getQuestionList()
 						.get(quest) instanceof Essay)) {
 			throw new Exception(
 					"MDFT46: Invalid positions provided or objects are not created yet");
 		}
 		return ((Essay) assessmentList.get(assmt).getSectionList().get(sect)
 				.getSubSectionList().get(subs).getQuestionList().get(quest))
 				.getCharactersAllowed();
 	}
 
 	public void setEssayTextAreaDimension(int[] positions,
 			Dimension textAreaDimension) throws Exception {
 		int assmt = positions[0] - 1;
 		int sect = positions[1] - 1;
 		int subs = positions[2] - 1;
 		int quest = positions[3] - 1;
 		if (!isAssessmentCreated(assmt)
 				|| !isSectionCreated(new int[] { assmt, sect, subs, quest })
 				|| !isSubSectionCreated(new int[] { assmt, sect, subs, quest })
 				|| !isQuestionCreated(new int[] { assmt, sect, subs, quest })
 				|| !(assessmentList.get(assmt).getSectionList().get(sect)
 						.getSubSectionList().get(subs).getQuestionList()
 						.get(quest) instanceof Essay)) {
 			throw new Exception(
 					"MDFT51: Invalid positions provided or objects are not created yet");
 		}
 		((Essay) assessmentList.get(assmt).getSectionList().get(sect)
 				.getSubSectionList().get(subs).getQuestionList().get(quest))
 				.setTextAreaDimension(textAreaDimension);
 		;
 	}
 
 	public Dimension getEssayTextAreaDimension(int[] positions) throws Exception {
 		int assmt = positions[0] - 1;
 		int sect = positions[1] - 1;
 		int subs = positions[2] - 1;
 		int quest = positions[3] - 1;
 		if (!isAssessmentCreated(assmt)
 				|| !isSectionCreated(new int[] { assmt, sect, subs, quest })
 				|| !isSubSectionCreated(new int[] { assmt, sect, subs, quest })
 				|| !isQuestionCreated(new int[] { assmt, sect, subs, quest })
 				|| !(assessmentList.get(assmt).getSectionList().get(sect)
 						.getSubSectionList().get(subs).getQuestionList()
 						.get(quest) instanceof Essay)) {
 			throw new Exception(
					"MDFT51: Invalid positions provided or objects are not created yet");
 		}
 		return ((Essay) assessmentList.get(assmt).getSectionList().get(sect)
 				.getSubSectionList().get(subs).getQuestionList().get(quest))
 				.getTextAreaDimension();
 	}
 
 	// </editor-fold>
 
 	public List<Assessment> getAssessmentList() {
 		return assessmentList;
 	}
 
 	public void setAssessmentList(List<Assessment> assessmentList) {
 		this.assessmentList = assessmentList;
 	}
 }
