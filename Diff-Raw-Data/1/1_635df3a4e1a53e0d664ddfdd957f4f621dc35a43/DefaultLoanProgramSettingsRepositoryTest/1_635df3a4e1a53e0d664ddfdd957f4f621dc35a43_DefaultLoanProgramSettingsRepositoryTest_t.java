 package org.gsoft.openserv.repositories.loan;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 
 import java.util.Date;
 
 import javax.annotation.Resource;
 
 import org.gsoft.openserv.domain.loan.DefaultLoanProgramSettings;
 import org.gsoft.openserv.domain.loan.LoanProgram;
 import org.gsoft.openserv.domain.loan.RepaymentStartType;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.test.annotation.Rollback;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.transaction.annotation.Transactional;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration("classpath:spring/application-context.xml")
 @Transactional(readOnly=true)
 public class DefaultLoanProgramSettingsRepositoryTest {
 	@Resource
 	private DefaultLoanProgramSettingsRepository defaultLoanProgramSettingsRepository;
 	
 	@Test
 	@Rollback
 	public void test() {
 		Date today = new Date();
 		DefaultLoanProgramSettings ltp = new DefaultLoanProgramSettings();
 		ltp.setDaysBeforeDueToBill(1);
 		ltp.setDaysLateForFee(1);
 		ltp.setEffectiveDate(today);
 		ltp.setGraceMonths(1);
 		ltp.setLateFeeAmount(1000);
 		LoanProgram loanType = new LoanProgram();
 		loanType.setLoanProgramID(20L);
 		ltp.setLoanProgram(loanType);
 		ltp.setMaximumLoanTerm(180);
 		ltp.setMinDaysToFirstDue(1);
 		ltp.setPrepaymentDays(1);
 		ltp.setRepaymentStartType(RepaymentStartType.FIRST_DISBUREMENT);
 		ltp.setVariableRate(true);
 		ltp = defaultLoanProgramSettingsRepository.save(ltp);
 		assertNotNull("Expected primary key to be generated", ltp.getDefaultLoanProgramSettingsID());
 		ltp = defaultLoanProgramSettingsRepository.findOne(ltp.getDefaultLoanProgramSettingsID());
 		assertEquals("Expected LoanType to be mortgage", ltp.getLoanProgram().getLoanProgramID(), loanType.getLoanProgramID());
 	}
 
 }
