 package hu.advancedweb.service.impl;
 
 import hu.advancedweb.model.ExamAnswer;
 import hu.advancedweb.service.base.ExamAnswerLocalServiceBaseImpl;
 
 import java.util.Date;
 import java.util.List;
 
 import com.google.common.base.Preconditions;
 import com.google.common.collect.Iterables;
 import com.liferay.portal.kernel.exception.SystemException;
 
 /**
  * The implementation of the exam answer local service.
  * 
  * <p>
  * All custom service methods should be put in this class. Whenever methods are added, rerun ServiceBuilder to copy their definitions into the
  * {@link hu.advancedweb.service.ExamAnswerLocalService} interface.
  * 
  * <p>
  * This is a local service. Methods of this service will not have security checks based on the propagated JAAS credentials because this service can only be accessed from within the
  * same VM.
  * </p>
  * 
  * @author Brian Wing Shun Chan
  * @see hu.advancedweb.service.base.ExamAnswerLocalServiceBaseImpl
  * @see hu.advancedweb.service.ExamAnswerLocalServiceUtil
  */
 public class ExamAnswerLocalServiceImpl extends ExamAnswerLocalServiceBaseImpl {
 	/*
 	 * NOTE FOR DEVELOPERS:
 	 * 
 	 * Never reference this interface directly. Always use {@link hu.advancedweb.service.ExamAnswerLocalServiceUtil} to access the exam answer local service.
 	 */
 
 	public ExamAnswer createExamAnswer(long companyId, long groupId, long userId, String answers, Date date, long examConfigId) throws SystemException {
 		List<ExamAnswer> list = getExamAnswerPersistence().findByCompanyId_GroupId_UserId_ExamConfigId(companyId, groupId, userId, examConfigId);
 
 		Preconditions.checkArgument(list.isEmpty());
 
 		ExamAnswer result = createExamAnswer(counterLocalService.increment());
 		result.setCompanyId(companyId);
 		result.setGroupId(groupId);
 		result.setUserId(userId);
 		result.setAnswers(answers);
 		result.setDate(date);
 		result.setExamConfigId(examConfigId);
 		result = updateExamAnswer(result);
 		return result;
 
 	}
 
 	public ExamAnswer getExamAnswer(long companyId, long groupId, long userId, long examConfigId) throws SystemException {
 		return Iterables.getFirst(getExamAnswerPersistence().findByCompanyId_GroupId_UserId_ExamConfigId(companyId, groupId, userId, examConfigId), null);
 	}
 }
