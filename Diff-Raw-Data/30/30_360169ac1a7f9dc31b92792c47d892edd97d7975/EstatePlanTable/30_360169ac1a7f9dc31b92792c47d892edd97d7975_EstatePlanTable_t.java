 package com.teag.EstatePlan;
 
 /**
  * @author Paul Stay
  * Created on May 30, 2005
  * Description - Genearte the Cash Flow Receipts for the Scenario 2 package.
  */
 import java.awt.Color;
 import java.awt.Font;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 import com.estate.constants.ToolTableTypes;
 import com.teag.analysis.CFCol;
 import com.teag.analysis.CFRow;
 import com.teag.analysis.CalcEstateTax;
 import com.teag.analysis.RetirementCashFlow;
 import com.teag.analysis.VariableCashFlow;
 import com.teag.bean.ABTrust;
 import com.teag.bean.AssetSqlBean;
 import com.teag.bean.BusinessBean;
 import com.teag.bean.DebtBean;
 import com.teag.bean.EPTAssetBean;
 import com.teag.bean.EstatePlanningToolBean;
 import com.teag.bean.FlpLPBean;
 import com.teag.bean.IlliquidBean;
 import com.teag.bean.InsuranceBean;
 import com.teag.bean.LlcLPBean;
 import com.teag.bean.LoadAssetBean;
 import com.teag.bean.MarriageBean;
 import com.teag.bean.NotePayableBean;
 import com.teag.bean.NotesBean;
 import com.teag.bean.PersonBean;
 import com.teag.bean.RealEstateBean;
 import com.teag.bean.VCFBean;
 import com.teag.client.Marriages;
 import com.teag.estate.CharliePlanTool;
 import com.teag.estate.LiquidAssetProtectionTool;
 import com.teag.estate.QprtTool;
 import com.teag.estate.SCINTool;
 import com.teag.estate.SIditTool;
 import com.teag.estate.SplitDollarTool;
 import com.teag.estate.TClat2;
 import com.teag.estate.TClatTool;
 import com.teag.sc.utils.LineObject;
 import com.teag.util.Function;
 import com.teag.util.TeagDefines;
 import com.teag.util.Utilities;
 import com.zcalc.zCalc;
 
 public class EstatePlanTable extends EstatePlanSqlBean {
 
     public static final int MAX_TABLE = 120;
     PersonBean client = new PersonBean();
     PersonBean spouse = new PersonBean();
     boolean isSingle = false;
     int retirementAge;
     int clientAge;
     int spouseAge;
     EstateSecurities securities = new EstateSecurities();
     EstateMunicipal municipal = new EstateMunicipal();
     GPAssets gpAssets = new GPAssets();
     LPAssets lpAssets = new LPAssets();
     LlcGPAssets llcAssets = new LlcGPAssets();
     EstateTools et = new EstateTools();
     // Charitable Deduction values
     EstateCharitableDeduction charitableDeduction = new EstateCharitableDeduction();
     double[] cDeductions = new double[MAX_TABLE];
     double[] charitableGift = new double[MAX_TABLE];
     double exemptionFix[] = new double[MAX_TABLE];
     // Save the depreciation for display, and for subtracting this value from
     // the tgaxable income!
     double[] depreciation = new double[MAX_TABLE];
     // Salary salary = new Salary();
     SocialSecurityPayment socialSecurity = new SocialSecurityPayment();
     Cash interestOnCash = new Cash();
     // LivingExpense livingExpense = new LivingExpense();
     // used for cashflow variables for both cash receipts, and cash
     // disbursements
     ArrayList<VariableCashFlow> vcfItems = new ArrayList<VariableCashFlow>();
     ArrayList<EstatePlanningToolBean> gratList;
     ArrayList<EstatePlanningToolBean> wptList;
     ArrayList<EstatePlanningToolBean> rpmList;
     ArrayList<EstatePlanningToolBean> iditList;
     ArrayList<EstatePlanningToolBean> gpList;
     ArrayList<EstatePlanningToolBean> crumList;
     ArrayList<EstatePlanningToolBean> qprtList;
     ArrayList<EstatePlanningToolBean> mgtList;
     ArrayList<EstatePlanningToolBean> clatList;
     ArrayList<EstatePlanningToolBean> tClatList;
     ArrayList<EstatePlanningToolBean> tClat2List;
     ArrayList<EstatePlanningToolBean> lapList;
     ArrayList<EstatePlanningToolBean> patList;
     ArrayList<EstatePlanningToolBean> erpList;
     ArrayList<CharliePlanTool> cplanList;
     ArrayList<EstatePlanningToolBean> scinList;
     ArrayList<SCINTool> scinArray;
     ArrayList<QprtTool> qprtToolList;
     ArrayList<EstatePlanningToolBean> lifeList;
     ArrayList<EstatePlanningToolBean> siditList;
     ArrayList<SIditTool> siditArray;
     ArrayList<EstatePlanningToolBean> splitList;
     // GratValues gratValues;
     GratCashFlow2 gratCashFlow;
     IditValues iditValues;
     RPMValues rpmValues;
     WPTValues wptValues;
     TClatValues tclatValues;
     PrivateAnnuityValues patValues;
     SplitDollarCashFlow splitValues;
     ClatValues clatValues;
     CrummeyValues crummValues;
     QprtValues qprtValues;
     MgtValues mgtValues;
     LifeToolValues lifeValues;
     Mortgage mortValues;
     RetirementCashFlow retirementCashFlow = new RetirementCashFlow();
     Map<String, Object> cashFlowMap = new LinkedHashMap<String, Object>();
     Map<String, Object> dMap = new LinkedHashMap<String, Object>();
     Map<String, Object> netWMap = new LinkedHashMap<String, Object>();
     Map<String, Object> estateMap = new LinkedHashMap<String, Object>();
     Map<String, Object> totalsMap = new LinkedHashMap<String, Object>();
     Map<String, Object> colorMap = new LinkedHashMap<String, Object>();
     Map<String, Object> fontMap = new LinkedHashMap<String, Object>();
     double taxableEstate[] = new double[MAX_TABLE];
     double netToFamily[] = new double[MAX_TABLE];
     double netToCharity[] = new double[MAX_TABLE];
     double finalToFamily[] = new double[MAX_TABLE];
     // Chart Data
     double chartFamily[] = new double[MAX_TABLE];
     double chartCharity[] = new double[MAX_TABLE];
     double chartSplit[] = new double[MAX_TABLE];
     double chartEstate[] = new double[MAX_TABLE];
     double chartTax[] = new double[MAX_TABLE];
     double tclat2Charity[] = new double[MAX_TABLE];
     double tclat2Family[] = new double[MAX_TABLE];
     boolean useTClat = true;
     boolean useTClat2 = true;
     double flp2Family[] = new double[MAX_TABLE];
     double excludePersonal[] = new double[MAX_TABLE];
     double taxDed[] = new double[MAX_TABLE];
     double eNetR[] = new double[MAX_TABLE];
     double marcCharity[] = new double[MAX_TABLE];
     double[] tclat;
     double[] tclatRI;
     double[] tclatCharity;
     double abEstate[] = new double[61];
     double nTrusts = 0;
     GiftTax clientGiftTax = new GiftTax();
     GiftTax spouseGiftTax = new GiftTax();
 
     public void addBonds(double value, int i) {
         double sRow[] = (double[]) cashFlowMap.get("Municipal Bonds");
         sRow[i] = value;
         cashFlowMap.remove("Municipal Bonds");
         cashFlowMap.put("Municipal Bonds", sRow);
     }
 
     public void addSecurities(double value, int i) {
         double sRow[] = (double[]) cashFlowMap.get("Int & Div on Securities Portfolio");
         sRow[i] = value;
         cashFlowMap.put("Int & Div on Securities Portfolio", sRow);
     }
 
     public void addToCRT(double value, int i) {
         double sRow[] = (double[]) cashFlowMap.get("crt");
         sRow[i] += value;
     }
 
     public void addToDisTotal(double value, int i) {
         double sRow[] = (double[]) dMap.get("dis-table");
         sRow[i] += value;
     }
 
     // BUild a place holder here for bonds as we will need to fill in the
     // interest as we work through the table
     public void buildBonds() {
         double sRow[] = new double[finalDeath + 1];
         for (int i = 0; i < finalDeath; i++) {
             sRow[i] = 0;
         }
         cashFlowMap.put("Municipal Bonds", sRow);
     }
 
     public void buildBusiness() {
         double annualIncome = 0;
         double annualIncomeGrowth = 0;
         EstatePlanAssets epa = new EstatePlanAssets(scenarioId);
 
         ArrayList<EPTAssetBean> bList = epa.getAssetTypes(2); // Business Type
         LoadAssetBean lab = new LoadAssetBean();
         Iterator<EPTAssetBean> i = bList.iterator();
         while (i.hasNext()) {
             EPTAssetBean eab = (EPTAssetBean) i.next();
             eab.setDbObject();
             double rValue = eab.getRemainingValue();
             if (rValue > 0) {
                 BusinessBean bb = (BusinessBean) lab.loadAssetBean(eab);
                 double pct = rValue / bb.getValue();
                 int aYear = finalDeath;
                 annualIncome = bb.getAnnualIncome() * pct;
                 annualIncomeGrowth = bb.getIncomeGrowth();
                 if (bb.getAld() != null) {
                     SimpleDateFormat df = new SimpleDateFormat("M/d/yyyy");
                     try {
                         Date ld = df.parse(bb.getAld());
                         Calendar cal = Calendar.getInstance();
                         cal.setTime(ld);
 
                         Date now = new Date();
                         Calendar currentCal = Calendar.getInstance();
                         currentCal.setTime(now);
 
                         aYear = cal.get(Calendar.YEAR) - currentCal.get(Calendar.YEAR);
                         double sellValue = rValue;
                         for (int j = 0; j <= aYear; j++) {
                             sellValue += sellValue * bb.getGrowthRate();
                         }
                         double tRow[] = new double[finalDeath + 1];
                         tRow[aYear] = sellValue;
                         cashFlowMap.put("Sell of Business", tRow);
                     } catch (Exception e) {
                         aYear = finalDeath;
                     }
                 }
 
                 double v = annualIncome * pct;
                 double sRow[] = new double[finalDeath + 1];
                 for (int j = 0; j < aYear; j++) {
                     sRow[j] = v;
                     v += v * annualIncomeGrowth;
                 }
                 cashFlowMap.put(bb.getDescription(), sRow);
             }
         }
     }
 
     public void buildCashErp() {
         Iterator<CharliePlanTool> itr = cplanList.iterator();
         while (itr.hasNext()) {
             CharliePlanTool cp = (CharliePlanTool) itr.next();
             cashFlowMap.put("ERP Pension", cp.getPension());
         }
     }
 
     public void buildCashNotes() {
         double sRow[] = new double[finalDeath + 1];
         boolean rFlag = false;
         EstatePlanAssets epa = new EstatePlanAssets(scenarioId);
         LoadAssetBean lab = new LoadAssetBean();
 
         ArrayList<EPTAssetBean> bList = epa.getAssetTypes(16); // Note
         // receivable
         // type
         Iterator<EPTAssetBean> i = bList.iterator();
         while (i.hasNext()) {
             EPTAssetBean eab = (EPTAssetBean) i.next();
             eab.setDbObject();
             double rValue = eab.getRemainingValue();
             NotesBean nb = (NotesBean) lab.loadAssetBean(eab);
             double iRate = nb.getInterestRate();
             String nType = nb.getNoteType();
             if (rValue > 0) {
                 rFlag = true;
                 if (nType.equalsIgnoreCase("I")) {
                     for (int k = 0; k < nb.getYears(); k++) {
                         sRow[k] += rValue * iRate;
                     }
                     sRow[nb.getYears() - 1] += rValue;
                 } else if (nType.equalsIgnoreCase("A")) {
                     double payment = Function.PMT(rValue, nb.getInterestRate() / nb.getPaymentsPerYear(), nb.getYears() * nb.getPaymentsPerYear()) * nb.getPaymentsPerYear();
                     for (int k = 0; k < nb.getYears(); k++) {
                         sRow[k] += payment;
                     }
                 } else {
                     sRow[nb.getYears() - 1] += nb.getLoanAmount();
                 }
             }
         }
         if (rFlag) {
             cashFlowMap.put("Note Receivable Payments", sRow);
         }
     }
 
     public void buildCashRealEstate() {
         double sRow[] = new double[finalDeath + 1];
         boolean rFlag = false;
         EstatePlanAssets epa = new EstatePlanAssets(scenarioId);
 
         ArrayList<EPTAssetBean> bList = epa.getAssetTypes(8); // Real Estate
         // Type
         LoadAssetBean lab = new LoadAssetBean();
         Iterator<EPTAssetBean> i = bList.iterator();
         while (i.hasNext()) {
             EPTAssetBean eab = (EPTAssetBean) i.next();
             eab.setDbObject();
             double rValue = eab.getRemainingValue();
             if (rValue > 0) {
                 rFlag = true;
                 RealEstateBean rb = (RealEstateBean) lab.loadAssetBean(eab);
                 double grossRents = rb.getGrossRents();
                 double grossGrowth = rb.getGrossRentsGrowth();
                 double expenses = rb.getOperatingExpenses();
                 double expGrowth = rb.getGrowthExpenses();
                 double income = grossRents - expenses;
                 double oValue = rb.getAssetValue();
                 double pct = 1.0;
 
                 if (oValue > 0) {
                     pct = rValue / oValue;
                 }
 
                 for (int k = 0; k < finalDeath; k++) {
                     sRow[k] += (income * pct);
                     grossRents += grossRents * grossGrowth;
                     expenses += expenses * expGrowth;
                     income = grossRents - expenses;
                 }
             }
         }
         if (rFlag) {
             cashFlowMap.put("Real Estate Income", sRow);
         }
     }
 
     public void buildCharitable() {
         double sRow[] = new double[finalDeath + 1];
         for (int i = 0; i < finalDeath; i++) {
             sRow[i] = cashFlowVars.getCharitableGifts();
         }
         dMap.put("Charitable Contributions", sRow);
     }
 
     // CRT = Cash Receipts Total
     public void buildCRT() {
         double sRow[] = new double[finalDeath + 1];
         for (int i = 0; i < finalDeath; i++) {
             sRow[i] = 0;
         }
         totalsMap.put("Total Cash Receipts", sRow);
     }
 
     // If this is a Grantor CLAT, than we need to add taxes, but we also get a
     // charitable Deduction
     public void buildClat() {
         if (clatValues.isGrantor()) {
             charitableDeduction.addDeduction(clatValues.getCharitableDeduction());
         }
         double sRow[] = new double[finalDeath + 1];
         for (int i = 0; i < finalDeath; i++) {
             sRow[i] = clatValues.getGrantorTax(i);
         }
         dMap.put("CLAT Taxes", sRow);
     }
 
     public void buildCrummey() {
         double sRow[] = new double[finalDeath + 1];
         for (int i = 0; i < crummValues.getTerm(); i++) {
             sRow[i] = crummValues.getAnnualGift();
         }
         dMap.put("Gift to Irrevocable (Crummey) Trust", sRow);
     }
 
     public void buildDebt() {
         EstatePlanAssets epa = new EstatePlanAssets(scenarioId);
         ArrayList<EPTAssetBean> bList = epa.getAssetTypes(4); // Debts
         LoadAssetBean lab = new LoadAssetBean();
         Iterator<EPTAssetBean> itr = bList.iterator();
         while (itr.hasNext()) {
             double sRow[] = new double[finalDeath + 1];
             EPTAssetBean eab = (EPTAssetBean) itr.next();
             eab.setDbObject();
             double rValue = eab.getRemainingValue();
             if (rValue > 0) {
                 DebtBean db = (DebtBean) lab.loadAssetBean(eab);
                 double value = db.getValue();
                 double interestRate = db.getInterestRate();
                 int years = (int) db.getLoanTerm();
                 double pmt = Function.PMT(value, interestRate, years);
                 for (int i = 0; i < years; i++) {
                     sRow[i] = pmt;
                 }
                 dMap.put(db.getDescription(), sRow);
             }
         }
     }
 
     public void buildDispSplit() {
 
         if (!splitList.isEmpty()) {
             double[] d = splitValues.getDisbursement();
             double sRow[] = new double[finalDeath + 1];
             double gRow[] = new double[finalDeath + 1];
             for (int i = 0; i < finalDeath; i++) {
                 sRow[i] = d[i];
             }
             gRow[0] = 400000;
             dMap.put("Split Dollar Trust Gift", gRow);
             dMap.put("Split Dollar Trust Payments", sRow);
         }
     }
     /*
      * public void buildVacation() { double sRow[] = new double[finalDeath+1];
      * for (int i = 0; i < finalDeath; i++) { sRow[i] =
      * cashFlowVars.getVacation(); } dMap.put("Vacation", sRow); }
      */
 
     public void buildDispErp() {
         Iterator<CharliePlanTool> itr = cplanList.iterator();
         while (itr.hasNext()) {
             CharliePlanTool cp = (CharliePlanTool) itr.next();
             dMap.put("Enhanced Retirement Plan Contr", cp.getContribution());
             double[] contr = cp.getContribution();
             dMap.put("ERP Tax (PS58)", cp.getCPs58Tax());
             dMap.put("ERP Tax (PS58)", cp.getSPs58Tax());
             for (int i = 0; i < finalDeath; i++) {
                 taxDed[i] += contr[i];
             }
         }
     }
 
     public void buildDisTotal() {
         double sRow[] = new double[finalDeath + 1];
         for (int i = 0; i < finalDeath; i++) {
             sRow[i] = 0;
         }
         dMap.put("dis-table", sRow);
     }
 
     public void buildErpSideFund() {
         Iterator<CharliePlanTool> itr = cplanList.iterator();
         while (itr.hasNext()) {
             CharliePlanTool cp = (CharliePlanTool) itr.next();
             buildRows("Endowment Fund", cp.getToFamily(), Font.BOLD, 1,
                     Color.orange);
         }
     }
 
     public void buildFamilyErp() {
         Iterator<CharliePlanTool> itr = cplanList.iterator();
         while (itr.hasNext()) {
             CharliePlanTool cp = (CharliePlanTool) itr.next();
             double life[] = new double[finalDeath];
             for (int i = 0; i < finalDeath; i++) {
                 life[i] = cp.getClientDeathBenefit() + cp.getSpouseDeathBenefit();
                 netToFamily[i] += life[i];
                 estateMap.put("Enhanced Retirement Plan Life Ins.", life);
             }
         }
     }
 
     public void buildFlpCash() {
         double sRow[] = new double[finalDeath + 1];
         boolean rFlag = false;
         EstatePlanAssets epa = new EstatePlanAssets(scenarioId);
         LoadAssetBean lab = new LoadAssetBean();
         ArrayList<EPTAssetBean> bList = epa.getAssetTypes(13); // LP shares
         Iterator<EPTAssetBean> i = bList.iterator();
         while (i.hasNext()) {
             EPTAssetBean eab = (EPTAssetBean) i.next();
             eab.setDbObject();
             double rValue = eab.getRemainingValue();
             FlpLPBean nb = (FlpLPBean) lab.loadAssetBean(eab);
             double iRate = nb.getAssetIncome();
             double iGrowth = nb.getAssetGrowth();
 
             double v = rValue;
             if (rValue > 0) {
                 rFlag = true;
                 for (int j = 0; j < finalDeath; j++) {
                     sRow[j] += v * iRate;
                     v = v * (1 + iGrowth);
                 }
             }
         }
 
         if (rFlag) {
             cashFlowMap.put("FLP LP Income", sRow);
         }
     }
 
     public void buildGiftTax() {
         double sRow[] = new double[finalDeath + 1];
         for (int i = 0; i < finalDeath; i++) {
             sRow[i] = 0;
         }
         dMap.put("Gift Tax (MGT, GRAT, etc)", sRow);
     }
 
     public void buildGP() {
         double sRow[] = new double[finalDeath + 1];
         for (int i = 0; i < finalDeath; i++) {
             sRow[i] = gpAssets.getInterestOnCash(i);
         }
         cashFlowMap.put("General Partner Rental Income", sRow);
     }
 
     public void buildSplitCF() {
         if (!splitList.isEmpty()) {
             double cf[] = splitValues.getCashFlow();
             double sRow[] = new double[finalDeath + 1];
             for (int i = 0; i < finalDeath; i++) {
                 sRow[i] = cf[i];
             }
 
             double tRow[] = new double[finalDeath + 1];
             tRow[0] = splitValues.getDisbursement()[0] + 400000; // Add in the gift as well.
             tRow[1] = splitValues.getDisbursement()[1];
             tRow[2] = splitValues.getDisbursement()[2];
             taxDed[0] += tRow[0];
             taxDed[1] += tRow[1];
             taxDed[2] += tRow[2];
             cashFlowMap.put("Split Dollar Bond Interest", sRow);
             cashFlowMap.put("Split Dollar Bond Sell", tRow);
         }
 
     }
 
     public void buildGrat() {
         double sRow[] = new double[finalDeath + 1];
         /*
          * for (int i = 0; i < finalDeath; i++) { sRow[i] =
          * gratValues.getGratPayment(i); }
          */
         LineObject gPay = gratCashFlow.getGratPayments();
         for (int i = 0; i < finalDeath; i++) {
             sRow[i] += gPay.getValue(i) * 1000.0;
         }
 
         cashFlowMap.put("Cash distributed from GRATs", sRow);
 
         // If we have assets that generate income than we need to addit here.
 
         ArrayList<LineObject> gLine = gratCashFlow.getAssetCashFlow();
 
         for (LineObject l : gLine) {
             double aRow[] = new double[finalDeath + 1];
             for (int i = 0; i < finalDeath; i++) {
                 aRow[i] = l.getValue(i) * 1000;
             }
             cashFlowMap.put(l.getDescription() + " income", aRow);
         }
     }
 
     public void buildIditGrat() {
         double sRow[] = new double[finalDeath + 1];
         for (int i = 0; i < finalDeath; i++) {
             sRow[i] = iditValues.getGratPayment(i) + iditValues.getNotePayment(i);
         }
         cashFlowMap.put("IDIT GRAT Payments", sRow);
     }
 
     public void buildIditNote() {
         /*
          * double sRow[] = new double[finalDeath + 1]; for (int i = 0; i <
          * finalDeath; i++) { sRow[i] = iditValues.getNotePayment(i); }
          * cashFlowMap.put("IDIT Note Payment", sRow);
          */
     }
 
     public void buildIlliquid() {
         EstatePlanAssets epa = new EstatePlanAssets(scenarioId);
 
         ArrayList<EPTAssetBean> bList = epa.getAssetTypes(5);
         LoadAssetBean lab = new LoadAssetBean();
         Iterator<EPTAssetBean> i = bList.iterator();
         while (i.hasNext()) {
             EPTAssetBean eab = (EPTAssetBean) i.next();
             eab.setDbObject();
             double v = eab.getRemainingValue();
             IlliquidBean ib = (IlliquidBean) lab.loadAssetBean(eab);
             double growth = ib.getAssetGrowth();
             double income = ib.getAssetIncome();
             double sRow[] = new double[finalDeath + 1];
             for (int j = 0; j < finalDeath; j++) {
                 v += v * growth;
                 sRow[j] = v * income;
             }
             cashFlowMap.put(ib.getAssetName(), sRow);
         }
     }
 
     public void buildIncomeTax() {
         double sRow[] = new double[finalDeath + 1];
         for (int i = 0; i < finalDeath; i++) {
             sRow[i] = 0;
         }
         dMap.put("Income Tax", sRow);
     }
 
     public void buildInterestOnCash() {
         double sRow[] = new double[finalDeath + 1];
         for (int i = 0; i < finalDeath; i++) {
             sRow[i] = interestOnCash.getInterestOnCash(i);
         }
         cashFlowMap.put("Interest On Cash", sRow);
     }
 
     public void buildLap() {
 
         double sRow[] = new double[finalDeath + 1];
         double dRow[] = new double[finalDeath + 1];
 
         if (!lapList.isEmpty()) {
             Iterator<EstatePlanningToolBean> itr = lapList.iterator();
             while (itr.hasNext()) {
                 EstatePlanningToolBean lap = (EstatePlanningToolBean) itr.next();
                 LiquidAssetProtectionTool l = new LiquidAssetProtectionTool();
                 l.setId(lap.getToolId());
                 l.read();
                 l.calculate();
                 for (int i = 0; i < finalDeath; i++) {
                     sRow[i] += l.getAnnualAnnuity();
                     dRow[i] += l.getLifePremium();
                 }
 
             }
             cashFlowMap.put("LAP Annuity Payment", sRow);
             dMap.put("LAP Life Ins. Premium", dRow);
 
         }
     }
 
     public void buildLLC() {
         double sRow[] = new double[finalDeath + 1];
         for (int i = 0; i < finalDeath; i++) {
             sRow[i] = llcAssets.getInterestOnCash(i);
         }
         cashFlowMap.put("LLC Managing Member Interest Income", sRow);
     }
 
     public void buildLlcCash() {
         double sRow[] = new double[finalDeath + 1];
         boolean rFlag = false;
         EstatePlanAssets epa = new EstatePlanAssets(scenarioId);
         LoadAssetBean lab = new LoadAssetBean();
         ArrayList<EPTAssetBean> bList = epa.getAssetTypes(15); // MMI shares
         Iterator<EPTAssetBean> i = bList.iterator();
         while (i.hasNext()) {
             EPTAssetBean eab = (EPTAssetBean) i.next();
             eab.setDbObject();
             double rValue = eab.getRemainingValue();
             LlcLPBean nb = (LlcLPBean) lab.loadAssetBean(eab);
             double iRate = nb.getAssetIncome();
             double iGrowth = nb.getAssetGrowth();
 
             double v = rValue;
             if (rValue > 0) {
                 rFlag = true;
                 for (int j = 0; j < finalDeath; j++) {
                     sRow[j] += v * iRate;
                     v = v * (1 + iGrowth);
                 }
             }
         }
 
         if (rFlag) {
             cashFlowMap.put("LLC Membert Interest Income", sRow);
         }
     }
 
     public void buildMgtTax() {
         if (!mgtList.isEmpty()) {
             double sRow[] = new double[finalDeath + 1];
             double growth = mgtValues.getAssetGrowth();
             double crumValue = crummValues.getAnnualGift();
             double lifePremium = crummValues.getLifePremium();
             double delTax = .2423;
 
             double mgtBalance = mgtValues.getAssetValue();
 
             // for (int i = 0; i < finalDeath; i++) {
             for (int i = 0; i < mgtValues.getTerm(); i++) {
                 double tGrowth = mgtBalance * growth;
                 mgtBalance = mgtBalance + tGrowth + crumValue - lifePremium;
                 sRow[i] = tGrowth * delTax;
             }
 
             dMap.put("Taxes from Grantor Multigeneration Trust", sRow);
         }
     }
 
     public void buildNetTotal() {
         double sRow[] = new double[finalDeath + 1];
         for (int i = 0; i < finalDeath; i++) {
             sRow[i] = getNetTotal(i);
             taxableEstate[i] += getNetTotal(i);
         }
         netWMap.put("END OF YEAR NET WORTH", sRow);
     }
 
     public void buildNetWorthNotes() {
         boolean rFlag = false;
         EstatePlanAssets epa = new EstatePlanAssets(scenarioId);
         ArrayList<EPTAssetBean> bList = epa.getAssetTypes(16);
         LoadAssetBean lab = new LoadAssetBean();
         Iterator<EPTAssetBean> itr = bList.iterator();
         while (itr.hasNext()) {
             double sRow[] = new double[finalDeath + 1];
             EPTAssetBean eab = (EPTAssetBean) itr.next();
             eab.setDbObject();
             double rValue = eab.getRemainingValue();
             if (rValue > 0) {
                 rFlag = true;
                 NotesBean n = (NotesBean) lab.loadAssetBean(eab);
                 if (n.getNoteType().equalsIgnoreCase("I")) {
                     for (int i = 0; i < n.getYears(); i++) {
                         sRow[i] = rValue;
                     }
                 } else if (n.getNoteType().equalsIgnoreCase("A")) {
                     double iRate = n.getInterestRate();
                     int years = n.getYears();
                     int paymentsPerYear = n.getPaymentsPerYear();
                     double payment = Function.PMT(rValue, iRate / paymentsPerYear, years * paymentsPerYear);
                     double balance = rValue;
                     for (int i = 0; i < years; i++) {
                         for (int j = 0; j < paymentsPerYear; j++) {
                             double ci = balance * (n.getInterestRate() / n.getPaymentsPerYear());
                             balance = balance - (payment - ci);
                         }
                         if (balance > 0) {
                             sRow[i] = balance;
                         } else {
                             sRow[i] = 0;
                         }
                     }
                 } else {
                     for (int i = 0; i < n.getYears() - 1; i++) {
                         sRow[i] = n.getLoanAmount();
                     }
                 }
                 if (rFlag) {
                     netWMap.put(n.getDescription(), sRow);
                 }
                 rFlag = false;
             }
         }
     }
 
     public void buildNetWorthNotesPayable() {
         boolean rFlag = false;
         EstatePlanAssets epa = new EstatePlanAssets(scenarioId);
         ArrayList<EPTAssetBean> bList = epa.getAssetTypes(17);
         LoadAssetBean lab = new LoadAssetBean();
         Iterator<EPTAssetBean> itr = bList.iterator();
         while (itr.hasNext()) {
             double sRow[] = new double[finalDeath + 1];
             EPTAssetBean eab = (EPTAssetBean) itr.next();
             eab.setDbObject();
             double rValue = eab.getRemainingValue();
             if (rValue > 0) {
                 rFlag = true;
                 NotePayableBean n = (NotePayableBean) lab.loadAssetBean(eab);
                 if (n.getNoteType().equalsIgnoreCase("I")) {
                     for (int i = 0; i < n.getYears(); i++) {
                         sRow[i] = -rValue;
                     }
                 } else {
                     double iRate = n.getInterestRate();
                     int years = n.getYears();
                     int paymentsPerYear = n.getPaymentsPerYear();
                     double payment = Function.PMT(rValue, iRate / paymentsPerYear, years * paymentsPerYear);
                     double balance = rValue;
                     for (int i = 0; i < years; i++) {
                         for (int j = 0; j < paymentsPerYear; j++) {
                             double ci = balance * (n.getInterestRate() / n.getPaymentsPerYear());
                             balance = balance - (payment - ci);
                         }
                         if (balance > 0) {
                             sRow[i] = -balance;
                         } else {
                             sRow[i] = 0;
                         }
                     }
                 }
                 if (rFlag) {
                     netWMap.put(n.getDescription(), sRow);
                 }
                 rFlag = false;
             }
         }
     }
 
     public void buildNotePayable() {
         double sRow[] = new double[finalDeath + 1];
         boolean rFlag = false;
         EstatePlanAssets epa = new EstatePlanAssets(scenarioId);
         LoadAssetBean lab = new LoadAssetBean();
 
         ArrayList<EPTAssetBean> bList = epa.getAssetTypes(17); // Note
         // receivable
         // type
         Iterator<EPTAssetBean> i = bList.iterator();
         while (i.hasNext()) {
             EPTAssetBean eab = (EPTAssetBean) i.next();
             eab.setDbObject();
             double rValue = eab.getRemainingValue();
             NotePayableBean nb = (NotePayableBean) lab.loadAssetBean(eab);
             double iRate = nb.getInterestRate();
             String nType = nb.getNoteType();
             if (rValue > 0) {
                 rFlag = true;
                 if (nType.equalsIgnoreCase("I")) {
                     for (int k = 0; k < nb.getYears(); k++) {
                         sRow[k] += rValue * iRate;
                     }
                     sRow[nb.getYears() - 1] += rValue;
                 } else if (nType.equalsIgnoreCase("A")) {
                     double payment = Function.PMT(rValue, nb.getInterestRate() / nb.getPaymentsPerYear(), nb.getYears() * nb.getPaymentsPerYear()) * nb.getPaymentsPerYear();
                     for (int k = 0; k < nb.getYears(); k++) {
                         sRow[k] += payment;
                     }
                 } else {
                     sRow[nb.getYears() - 1] += nb.getLoanAmount();
                 }
             }
         }
         if (rFlag) {
             dMap.put("Note Payments", sRow);
         }
     }
 
     public void buildPat() {
         double sRow[] = new double[finalDeath + 1];
         for (int i = 0; i < finalDeath; i++) {
             sRow[i] = patValues.annuity;
         }
         cashFlowMap.put("Private Annuity Payment", sRow);
     }
 
     public void buildPatCapGains() {
         double sRow[] = new double[finalDeath + 1];
         for (int i = 0; i < finalDeath; i++) {
             sRow[i] = patValues.capGains[i];
         }
         dMap.put("Capital Gains on Private Annuity", sRow);
     }
 
     public void buildRealEstate() {
         double sRow[] = new double[finalDeath + 1];
         boolean rFlag = false;
         EstatePlanAssets epa = new EstatePlanAssets(scenarioId);
 
         ArrayList<EPTAssetBean> bList = epa.getAssetTypes(8); // Real Estate
         // Type
         LoadAssetBean lab = new LoadAssetBean();
         Iterator<EPTAssetBean> i = bList.iterator();
         while (i.hasNext()) {
             EPTAssetBean eab = (EPTAssetBean) i.next();
             eab.setDbObject();
             double rValue = eab.getRemainingValue();
             if (rValue > 0) {
                 rFlag = true;
                 RealEstateBean rb = (RealEstateBean) lab.loadAssetBean(eab);
                 double payment = rb.getLoanPayment();
                 double term = rb.getLoanTerm();
                 int years = (int) Math.floor(term / 12.0) + 1;
                 if (years > finalDeath) {
                     years = finalDeath;
                 }
                 for (int k = 0; k < years; k++) {
                     sRow[k] += payment;
                 }
                 if (rb.getDepreciationValue() > 0 && rb.getDepreciationYears() > 0) {
                     double dep = rb.getDepreciationValue();
                     double sv = rb.getSalvageValue();
                     int depyears = (int) rb.getDepreciationYears();
                     double dValue = (dep - sv) / depyears;
 
                     for (int j = 0; j < depyears; j++) {
                         depreciation[j] += dValue;
                     }
                 }
             }
         }
         // Fix depreciation here
         for(int x=0; x < finalDeath; x++){
             depreciation[x] += cashFlowVars.getDepreciation();
         }
 
         if (rFlag) {
             cashFlowMap.put("Real Estate Mortgage Principle Payments", sRow);
         }
     }
 
     public void buildRetirement() {
         double sRow[] = new double[finalDeath + 1];
         double rRow[] = retirementCashFlow.getDispersements();
         boolean rFlag = false;
 
         for (int i = 0; i < finalDeath; i++) {
             double v = rpmValues.getDistribution(i) + rRow[i];
             sRow[i] = v;
             if (v > 1000) {
                 rFlag = true;
             }
         }
         if (rFlag) {
             cashFlowMap.put("Retirement Distributions", sRow);
         }
     }
 
     public void buildRows(String title, double data[], int weight, int indent) {
         boolean rFlag = false;
         Color color = (Color) colorMap.get(title);
         if (color == null) {
             color = Color.BLACK;
         }
 
         CFRow r = new CFRow();
         r.setHeader(title);
         r.setIndentLevel(indent);
         r.setTextFill(0); // Left
         r.setFontWeight(weight);
         r.setFontColor(color);
 
         for (int i = 0; i < finalDeath; i++) {
             double v = data[i];
             CFCol c = new CFCol();
             if (v < 0 || v > 0) {
                 rFlag = true;
             }
             if (v < 0) {
                 c.setStrValue(formatNumber(v));
             } else {
                 c.setStrValue(formatNumber(v));
             }
             c.setFontColor(color);
             c.setTextFill(3);
             c.setFontWeight(weight);
             r.addCol(c);
         }
         if (rFlag) {
             rows.add(r);
         }
     }
 
     public void buildRows(String title, double data[], int weight, int indent,
             Color color) {
         boolean rFlag = false;
 
         CFRow r = new CFRow();
         r.setHeader(title);
         r.setIndentLevel(indent);
         r.setTextFill(0); // Left
         r.setFontWeight(weight);
         r.setFontColor(color);
 
         for (int i = 0; i < finalDeath; i++) {
             double v = data[i];
             CFCol c = new CFCol();
             if (v < 0 || v > 0) {
                 rFlag = true;
             }
             if (v < 0) {
                 c.setStrValue(formatNumber(v));
             } else {
                 c.setStrValue(formatNumber(v));
             }
             c.setFontColor(color);
             c.setTextFill(3);
             c.setFontWeight(weight);
             r.addCol(c);
         }
         if (rFlag) {
             rows.add(r);
         }
     }
 
     public void buildRpmDis() {
         double sRow[] = new double[finalDeath + 1];
         for (int i = 0; i < finalDeath; i++) {
             sRow[i] = rpmValues.getToTrust(i);
         }
         dMap.put("RPM Trust", sRow);
     }
 
     public void buildRpmTax() {
         double sRow[] = new double[finalDeath + 1];
         for (int i = 0; i < finalDeath; i++) {
             sRow[i] = rpmValues.getTax(i);
         }
         dMap.put("RPM Taxes", sRow);
     }
 
     public void buildScin() {
         Iterator<SCINTool> itr = scinArray.iterator();
         while (itr.hasNext()) {
             SCINTool s = (SCINTool) itr.next();
             double sRow[] = s.getPayment();
             cashFlowMap.put("SCIN Payment", sRow);
         }
     }
 
     public void buildScinTax() {
         Iterator<SCINTool> itr = scinArray.iterator();
         while (itr.hasNext()) {
             SCINTool scin = (SCINTool) itr.next();
             double sRow[] = scin.getCapGains();
             dMap.put("SCIN Capital Gains", sRow);
             double tRow[] = scin.getTaxFree();
             for (int i = 0; i < scin.getTerm(); i++) {
                 taxDed[i] += tRow[i];
                 taxDed[i] += sRow[i];
             }
         }
     }
 
     // Build a place holder here for securities int/dividends as we need to fill
     // in the interest later
     public void buildSecurities() {
         double sRow[] = new double[finalDeath + 1];
         for (int i = 0; i < finalDeath; i++) {
             sRow[i] = 0;
         }
         cashFlowMap.put("Int & Div on Securities Portfolio", sRow);
     }
 
     public void buildSidit() {
         Iterator<SIditTool> itr = siditArray.iterator();
         while (itr.hasNext()) {
             SIditTool s = (SIditTool) itr.next();
             double sRow[] = s.getNotePayment();
             cashFlowMap.put("IDIT Note Payment", sRow);
             // Idit payments are non taxable.
             for (int i = 0; i < sRow.length; i++) {
                 taxDed[i] += sRow[i];
             }
         }
     }
 
     public void buildCharitableCont(int year) {
         // Charitable Deductions
         double tithing = cashFlowVars.getCharity();
         if (tithing > 0) {
             double crTotal = getCRTotal(year);
             charitableGift[year] += crTotal * tithing;
             //netToCharity[i] += tRow[i];
         }
     }
 
     public void buildSiditTax() {
         Iterator<SIditTool> itr = siditArray.iterator();
         while (itr.hasNext()) {
             SIditTool s = (SIditTool) itr.next();
             double sRow[] = s.getTax();
             dMap.put("Grantor IDIT Income tax", sRow);
         }
     }
 
     public void buildSocialSecurity() {
         double sRow[] = new double[finalDeath + 1];
         for (int i = 0; i < finalDeath; i++) {
             sRow[i] = socialSecurity.getSocialPayment(i);
         }
         cashFlowMap.put("Social Security", sRow);
     }
 
     public void buildWpt() {
         double sRow[] = new double[finalDeath + 1];
         for (int i = 0; i < finalDeath; i++) {
             sRow[i] = wptValues.getPayment(i);
         }
         cashFlowMap.put("CRUT(s) Distributions", sRow);
         // Creat the Charitable Deduction that we can get for this transfer
         charitableDeduction.addDeduction(wptValues.getCharitableDeduction());
     }
 
 
 
     public void calcABEstate() {
         ABTrust ab = new ABTrust();
         ab.query(client.getId());
 
         if (ab.getUsed().equals("B") || ab.getUsed().equals("2")) {
             Date now = new Date();
             Calendar cal = Calendar.getInstance();
             cal.setTime(now);
             int year = cal.get(Calendar.YEAR);
             nTrusts = ab.getAfterTrusts();
 
             zCalc zc = new zCalc();
             zc.StartUp();
             /*
              * This is somewhat tricky, the AB trust actually only gives the exemption
              * We want to show that both exemptions are used, but we only subtract one
              * of them.
              */
             for (int i = 0; i < finalDeath; i++) {
                 double exempt = zc.zAPPEXCLUSION(year, 0, 0, 0);
                 abEstate[i] = -exempt * nTrusts;
 
                 if(nTrusts > 1){
                     exemptionFix[i] = exempt;
                 } else {
                     exemptionFix[i] = 0;
                 }
 
                 taxableEstate[i] += -exempt;
                 year++;
             }
             zc.ShutDown();
         }
     }
 
     // Need to fill this in
     public void calcGiftTax() {
         /*
          * clientGiftTax.setDbobj(dbobj); clientGiftTax.finalDeath = finalDeath;
          * clientGiftTax.ownerId = clientBean.getPrimaryId();
          * clientGiftTax.crumGift = (crummValues.getAnnualGift()) / 2.0;
          * clientGiftTax.crumTerm = crummValues.getTerm(); clientGiftTax.init();
          * double giftValue = 0; if (year == 0) { giftValue =
          * gratValues.clientGiftTax + qprtValues.getClientGift() +
          * rpmValues.getToTrust(0) / 2 + mgtValues.getAssetValue() / 2; } else {
          * giftValue = rpmValues.getToTrust(year) / 2; }
          *
          * double gift = clientGiftTax.calculate(giftValue, year);
          *
          * spouseGiftTax.setDbobj(dbobj); spouseGiftTax.finalDeath = finalDeath;
          * spouseGiftTax.ownerId = clientBean.getPrimaryId();
          * spouseGiftTax.crumGift = (crummValues.getAnnualGift()) / 2.0;
          * spouseGiftTax.crumTerm = crummValues.getTerm(); spouseGiftTax.init();
          * if (year == 0) { giftValue = gratValues.spouseGiftTax +
          * qprtValues.getSpouseGift() + rpmValues.getToTrust(0) / 2 +
          * mgtValues.getAssetValue() / 2; } else { giftValue =
          * rpmValues.getToTrust(year) / 2; }
          *
          * gift += spouseGiftTax.calculate(giftValue, year);
          *
          * double sRow[] = (double[]) dMap.get("Gift Tax (MGT, GRAT, etc)");
          * sRow[year] = gift;
          */
     }
 
     public void calcIncomeTax(int year) {
         double iRow[] = (double[]) dMap.get("Income Tax");
         double totalCash = getCRTotal(year) - getVcfTax(year) - patValues.taxFree[year] - taxDed[year];
         ;
         // - rpmValues.getDistribution(year);
         double t1 = calcTax(totalCash, year);
         if (t1 > 0) {
             iRow[year] = t1;
         } else {
             iRow[year] = 0;
         }
     }
 
     public double calcTax(double totalCash, int year) {
         Date now = new Date();
         Calendar cal = Calendar.getInstance();
         cal.setTime(now);
         int rYear = cal.get(Calendar.YEAR);
 
         CalcCashFlowTax ccf = new CalcCashFlowTax();
         ccf.totalCashInflows = totalCash;
         ccf.crutCapGains = wptValues.getValue(year) * (wptValues.payOutRate - wptValues.interestRate);
         ccf.stateIncomeTaxRate = cashFlowVars.getStateTaxRate();
         ccf.socialSecurity = socialSecurity.getSocialPayment(year);
         ccf.bonds = municipal.getBeginBalance(year) * municipal.getInterestRate();
 
         // We need to fix this, the CRT does not get a charitable deduction for
         // all of the years!
         double charity = cashFlowVars.getCharitableGifts() + this.getVcfCharity(year) + charitableGift[year];
 
         charitableDeduction.addDeduction(charity);
 
         ccf.stateAGI = (totalCash) - ccf.socialSecurity - ccf.crutCapGains - ccf.bonds - depreciation[year];
 
         // double maxAllowed = ccf.stateAGI * .30;
         // ccf.charitableDed = Math.min(maxAllowed, charity);
 
         // this is the new way to calculate the charitable deduction, we keep
         // things for at least 5 years.
         ccf.charitableDed = charitableDeduction.getDed(ccf.stateAGI);
         // We subtract out the charitableGift part here, as we have already included in in the cash
         // flow, as per Henry's direction.
         cDeductions[year] = ccf.charitableDed - charitableGift[year] - getVcfCharity(year);
         ccf.capTaxRate = .20;
 
         ccf.calculate(rYear + year);
         return ccf.totalTax;
 
     }
 
     public void cashFlow() {
         // Ok lets build cash flow and disbursements tables;
         // cashFlowVariables();
         buildSocialSecurity();
         buildInterestOnCash();
         buildClat();
         buildBonds();
         buildSecurities();
         buildRetirement();
         buildIlliquid();
         buildCashErp();
         buildCashRealEstate();
         buildBusiness();
         buildCashNotes();
         buildLlcCash();
         buildFlpCash();
         buildWpt();
         buildGrat();
         buildSplitCF();
         // buildIditNote();
         buildIditGrat();
         buildGP();
         buildLLC();
         buildLap();
         buildPat();
         buildCRT();
         buildSidit();
         buildScin();
 
         // disbursementVariables();
         buildRealEstate();
         buildRpmDis();
         buildCrummey();
         buildDebt();
         buildNotePayable();
         buildDispErp();
         buildDispSplit();
         // buildGiftTax();
         buildRpmTax();
         buildMgtTax();
         buildPatCapGains();
         buildScinTax();
         buildSiditTax();
         buildIncomeTax();
         // First build the initial securities and bonds and add then interest on
         // both to the cashflow tables.
 
         double sBalance = securities.getBeginBalance(0);
         double sInt = securities.getInterestRate();
         addSecurities(sBalance * sInt, 0);
 
         double bBalance = municipal.getBeginBalance(0);
         double bInt = municipal.getInterestRate();
         addBonds(bBalance * bInt, 0);
         calcGiftTax();
 
         double crt = getCRTotal(0);
         buildCharitableCont(0);
         calcIncomeTax(0);
         double dis = getDisTotal(0) + charitableGift[0];
 
         double excessCash = crt - dis;
         /*
          * if( sBalance > 0 && excessCash < 0) {
          * securities.addCashFlow(0,excessCash); } else {
          * securities.addCashFlow(0, excessCash .7); municipal.addCashFlow(0,
          * excessCash .3); }
          */
 
         securities.addCashFlow(0, excessCash);
         municipal.addCashFlow(0, 0);
 
         // OK put 70% of excess into securities, and 30% in Bonds
         // Loop through to finalDeath and update fields where needed.
 
         for (int i = 1; i < finalDeath; i++) {
             sBalance = securities.getBeginBalance(i);
             addSecurities(sBalance * sInt, i);
             bBalance = municipal.getBeginBalance(i);
             addBonds(bBalance * bInt, i);
             calcGiftTax();
             crt = getCRTotal(i);
             buildCharitableCont(i);
             calcIncomeTax(i);
             dis = getDisTotal(i) + charitableGift[i];
             excessCash = crt - dis;
             /*
              * if( sBalance > 0 && excessCash < 0) {
              * securities.addCashFlow(i,excessCash); } else {
              * securities.addCashFlow(i, excessCash .7);
              * municipal.addCashFlow(i, excessCash .3); }
              */
             securities.addCashFlow(i, excessCash);
             municipal.addCashFlow(i, 0);
         }
         CFRow r = new CFRow();
         r.setHeader("I. CASH FLOW");
         r.setIndentLevel(0);
         r.setTextFill(0); // Left
         r.setFontSize(2);
         r.setFontWeight(Font.BOLD);
 
         rows.add(r);
 
         CFRow r2 = new CFRow();
         r2.setHeader("CASH RECEIPTS:");
         r2.setIndentLevel(1);
         r2.setTextFill(0); // Left
         r2.setFontSize(1);
         r2.setFontWeight(Font.BOLD);
 
         rows.add(r2);
 
         for (VariableCashFlow v : vcfItems) {
             if (v.getCfType().equalsIgnoreCase("C")) {
                 buildRows(v.getDescription(), v.getVTable(), Font.PLAIN, 1);
             }
         }
 
         for (String i : cashFlowMap.keySet()) {
             double sRow[] = (double[]) cashFlowMap.get(i);
             buildRows(i, sRow, Font.PLAIN, 1);
         }
 
         /*
          * Set<String> keys = cashFlowMpap.keySet(); Iterator<Object> kItr =
          * keys.iterator();
          *
          * while (kItr.hasNext()) { String entry = (String) kItr.next(); double
          * sRow[] = (double[]) cashFlowMap.get(entry); buildRows(entry, sRow,
          * Font.PLAIN, 1); }
          */
 
         // Build the total cash recipts
         double zRow[] = new double[finalDeath + 1];
         for (int i = 0; i < finalDeath; i++) {
             zRow[i] = getCRTotal(i);
         }
         buildRows("Total Cash Recipts", zRow, Font.BOLD | Font.ITALIC, 2);
 
         CFRow r4 = new CFRow();
         r4.setHeader("DISBURSEMENTS:");
         r4.setIndentLevel(1);
         r4.setTextFill(0); // Left
         r4.setFontSize(1);
         r4.setFontWeight(Font.BOLD);
 
         rows.add(r4);
 
         for (VariableCashFlow v : vcfItems) {
             if (v.getCfType().equalsIgnoreCase("D")) {
                 buildRows(v.getDescription(), v.getVTable(), Font.PLAIN, 1);
             }
         }
         // Output the charitable deduciton and depreciation here, they are not used in the totals.
         // Add in the Charitable Contributions
         buildRows("Charitable Contributions", charitableGift, Font.PLAIN, 1);
 
         for (String entry : dMap.keySet()) {
             double sRow[] = (double[]) dMap.get(entry);
             buildRows(entry, sRow, Font.PLAIN, 1);
         }
 
 
         boolean cflag = false;
 
         CFRow dedRow = new CFRow();
         dedRow.setHeader("Charitable Deductions");
         dedRow.setIndentLevel(1);
         dedRow.setTextFill(0);
         dedRow.setFontSize(0);
 
         for (int i = 0; i < finalDeath; i++) {
             CFCol c = new CFCol();
             c.setStrValue("[" + formatNumber(cDeductions[i]) + "]");
             c.setTextFill(3);
             dedRow.addCol(c);
             if (cDeductions[i] > 0) {
                 cflag = true;
             }
         }
 
         if (cflag) {
             rows.add(dedRow);
         }
 
         boolean dFlag = false;
         CFRow depRow = new CFRow();
         depRow.setHeader("Depreciation");
         depRow.setIndentLevel(1);
         depRow.setTextFill(0);
         depRow.setFontSize(0);
 
         for (int i = 0; i < finalDeath; i++) {
             if (depreciation[i] > 0) {
                 dFlag = true;
             }
             CFCol c = new CFCol();
             c.setStrValue("[" + formatNumber(depreciation[i]) + "]");
             c.setTextFill(3);
             depRow.addCol(c);
         }
 
         if (dFlag) {
             rows.add(depRow);
         }
 
         /*
          * keys = dMap.keySet(); kItr = keys.iterator();
          *
          * while (kItr.hasNext()) { String entry = (String) kItr.next(); double
          * sRow[] = (double[]) dMap.get(entry); buildRows(entry, sRow,
          * Font.PLAIN, 1); }
          */
 
         // Build the total cash recipts
         double yRow[] = new double[finalDeath + 1];
         for (int i = 0; i < finalDeath; i++) {
             yRow[i] = getDisTotal(i) + charitableGift[i];
         }
 
         buildRows("Total Disbursements", yRow, Font.BOLD | Font.ITALIC, 2);
 
         CFRow r5 = new CFRow();
         r5.setHeader("EXCESS CASH FLOW:");
         r5.setIndentLevel(1);
         r5.setTextFill(0); // Left
         r5.setFontSize(1);
         r5.setFontWeight(Font.BOLD);
 
         double xRow[] = new double[finalDeath + 1];
 
         for (int i = 0; i < finalDeath; i++) {
             xRow[i] = zRow[i] - yRow[i];
             CFCol c = new CFCol();
 
             if (xRow[i] < 0) {
                 c.setStrValue(formatNumber(xRow[i]));
                 c.setFontColor(Color.red);
             } else {
                 c.setStrValue(formatNumber(xRow[i]));
             }
             c.setFontWeight(Font.BOLD);
             c.setTextFill(3);
             r5.addCol(c);
         }
 
         rows.add(r5);
 
         // Do Securities and Bonds
         CFRow r6 = new CFRow();
         r6.setHeader("II. SECURITIES PORTFOLIO");
         r6.setIndentLevel(0);
         r6.setTextFill(0); // Left
         r6.setFontSize(2);
         r6.setFontWeight(Font.BOLD);
         rows.add(r6);
 
         double wRow[] = new double[finalDeath + 1];
         for (int i = 0; i < finalDeath; i++) {
             wRow[i] = securities.getBeginBalance(i);
         }
 
         double vRow[] = new double[finalDeath + 1];
         for (int i = 0; i < finalDeath; i++) {
             crt = getCRTotal(i);
             dis = getDisTotal(i) + charitableGift[i];
             double eCash = crt - dis;
             vRow[i] = eCash;
         }
 
         double uRow[] = new double[finalDeath + 1];
         for (int i = 0; i < finalDeath; i++) {
             uRow[i] = wRow[i] * securities.getGrowthRate();
         }
 
         double tRow[] = new double[finalDeath + 1];
         for (int i = 0; i < finalDeath; i++) {
             tRow[i] = securities.getEndBalance(i);
         }
 
         buildRows("BEG. BALANCE SECURITIES PORTFOLIO", wRow, Font.BOLD, 1);
         buildRows("Growth", uRow, Font.BOLD, 2);
         buildRows("Excess Cash", vRow, Font.BOLD, 2);
         buildRows("ENDING BALANCE SECURITIES PORTFOLIO", tRow, Font.BOLD, 1);
     }
 
     public void cashFlowVariables() {
         for (VariableCashFlow v : vcfItems) {
             if (v.getCfType().equals("C")) {
                 double nRow[] = new double[MAX_TABLE];
                 double sRow[] = v.getVTable();
                 for (int i = 0; i < MAX_TABLE; i++) {
                     nRow[i] = sRow[i];
                 }
                 cashFlowMap.put(v.getDescription(), nRow);
             }
         }
     }
 
     public void disbursementVariables() {
         for (VariableCashFlow v : vcfItems) {
             if (v.getCfType().equals("D")) {
                 dMap.put(v.getDescription(), v.getVTable());
             }
         }
     }
 
     public void eABTrust() {
         estateMap.put("Exemptions(" + Integer.toString((int) nTrusts) + ")",
                 abEstate);
     }
 
     public void eCharityFromClat() {
         double zRow[] = new double[finalDeath + 1];
         for (int i = 0; i < finalDeath; i++) {
             zRow[i] = clatValues.getClatCharity(i);
         }
         estateMap.put("Amount to Charity from CLAT", zRow);
     }
 
     public void eCharityFromCrut() {
         double zRow[] = new double[finalDeath + 1];
         for (int i = 0; i < finalDeath; i++) {
             zRow[i] = wptValues.getValue(i);
         }
 
         estateMap.put("Amount to Charity from CRT", zRow);
     }
 
     public void eCharityFromTClat() {
         if (useTClat) {
             double zRow[] = new double[finalDeath + 1];
             for (int i = 0; i < finalDeath; i++) {
                 zRow[i] = tclatCharity[i];
             }
             estateMap.put("PV of AMT to Charity from Test. CLAT", zRow);
         }
     }
 
     public void eCharityFromTClat2() {
         estateMap.put("Amount to Charity from TCLAT", tclat2Charity);
     }
 
     public void eClat() {
         double sRow[] = new double[finalDeath + 1];
         for (int i = 0; i < finalDeath; i++) {
             sRow[i] = clatValues.getClatValue(i);
             netToFamily[i] += sRow[i];
         }
         estateMap.put("Charitable Lead Annuity Trust (CLAT)", sRow);
     }
 
     public void eCrumLife() {
         double sRow[] = new double[finalDeath + 1];
 
         for (int i = 0; i < finalDeath; i++) {
             sRow[i] = lifeValues.getFaceValue(i);
             netToFamily[i] += lifeValues.getFaceValue(i);
         }
         estateMap.put("Life Insurance Trust", sRow);
     }
 
     public void eCrummey() {
         double gift = crummValues.getAnnualGift();
         double zRow[] = new double[finalDeath + 1];
         if (crummValues.useMgt) {
             zRow[0] = gift;
             netToFamily[0] += zRow[0];
             for (int i = 1; i < finalDeath; i++) {
                 zRow[i] = (zRow[i - 1] * 1.06) + gift;
                 netToFamily[i] += zRow[i];
             }
             estateMap.put("Irrevocable Trust(Receivables @6%)", zRow);
         } else {
             zRow[0] = crummValues.getDeathBenefit();
             netToFamily[0] += zRow[0];
             for (int i = 1; i < finalDeath; i++) {
                 zRow[i] = crummValues.getDeathBenefit();
                 netToFamily[i] += zRow[i];
             }
             estateMap.put("Irrevocable Trust (Life)", zRow);
         }
     }
 
     public void eFamilyGrat() {
         double sRow[] = new double[finalDeath + 1];
 
         LineObject toFam = gratCashFlow.getGratToFamily();
         for (int i = 0; i < finalDeath; i++) {
             sRow[i] += toFam.getValue(i) * 1000.0;
             netToFamily[i] += sRow[i];
         }
 
         estateMap.put("Grantor Retained Annuity Trust (Family)", sRow);
     }
 
     public void eFamilyIdit() {
         double sRow[] = new double[finalDeath + 1];
         for (int i = 0; i < finalDeath; i++) {
             sRow[i] = iditValues.getToFamily(i);
             netToFamily[i] += sRow[i];
         }
 
         estateMap.put("Intentionaly Defective Irrevocable Trust", sRow);
     }
 
     public void eFamilyRPM() {
         double sRow[] = new double[finalDeath + 1];
         for (int i = 0; i < finalDeath; i++) {
             sRow[i] = rpmValues.getToFamily(i);
             netToFamily[i] += sRow[i];
         }
         estateMap.put("Retirement Plan Maximizer", sRow);
     }
 
     public void eFamilySplit() {
         if (!splitList.isEmpty()) {
             double sRow[] = new double[finalDeath + 1];
             double fam[] = splitValues.getToFamily();
 
             for (int i = 0; i < finalDeath; i++) {
                 sRow[i] = fam[i];
                 netToFamily[i] += fam[i];
             }
             estateMap.put("MGT - Split Dollar", sRow);
         }
     }
 
     public void eFamilySIdit() {
         double sRow[] = new double[finalDeath + 1];
         for (SIditTool sidit : siditArray) {
             double prin = sidit.getFmv();
             double growth = sidit.getAssetGrowth();
             double income = sidit.getAssetIncome();
             double pay[] = sidit.getNotePayment();
             double premium = sidit.getLifePremium();
             double lifeYears = sidit.getLifePremiumYears();
             double noteBalance[] = sidit.getNoteBalance();
 
             for (int i = 0; i < finalDeath; i++) {
                 if (i >= lifeYears) {
                     premium = 0;
                 }
                 prin = prin * (1 + growth + income) - pay[i] - premium;
                 sRow[i] = prin + sidit.getLifeDeathBenefit() - noteBalance[i];
                 netToFamily[i] += sRow[i];
             }
             estateMap.put("IDIT", sRow);
         }
     }
 
     public void eFLP() {
         // Need to subtract discount from FLP LP and add additional GP if
         // needed.
         double sRow[] = new double[finalDeath + 1];
         boolean rFlag = false;
         EstatePlanAssets epa = new EstatePlanAssets(scenarioId);
         LoadAssetBean lab = new LoadAssetBean();
         ArrayList<EPTAssetBean> bList = epa.getAssetTypes(13); // LP shares
         Iterator<EPTAssetBean> i = bList.iterator();
         while (i.hasNext()) {
             EPTAssetBean eab = (EPTAssetBean) i.next();
             eab.setDbObject();
             double rValue = eab.getRemainingValue();
             FlpLPBean nb = (FlpLPBean) lab.loadAssetBean(eab);
             double discount = nb.getDiscount();
             double iGrowth = nb.getAssetGrowth();
 
             double v = rValue;
             if (rValue > 0) {
                 rFlag = true;
                 for (int j = 0; j < finalDeath; j++) {
                     sRow[j] += -(v * (1 + iGrowth)) * discount;
                     taxableEstate[j] += sRow[j];
                     flp2Family[j] += -sRow[j];
                     v = v * (1 + iGrowth);
                 }
             }
         }
 
         if (rFlag) {
             estateMap.put("FLP LP discount", sRow);
         }
     }
 
     public void eFlp2Family() {
         for (int i = 0; i < finalDeath; i++) {
             netToFamily[i] += flp2Family[i];
         }
         estateMap.put("FLP Non Taxable Discount", flp2Family);
     }
 
     // Add in the note to the estate distribution
     // We don't need to do this sice we have already included it in the estate in the net worth
     // part.
     public void eSidit() {
         /*
         double sRow[] = new double[finalDeath + 1];
         // Add in the NoteBalance here for each IDIT
         for (SIditTool s : siditArray) {
         double balance[] = s.getNoteBalance();
         for (int i = 0; i < finalDeath; i++) {
         sRow[i] += balance[i];
         taxableEstate[i] += balance[i];
         }
         }
         estateMap.put("Idit Note Balance", sRow);
          */
     }
 
     public void eGrat() {
         double sRow[] = new double[finalDeath + 1];
 
         LineObject ed = gratCashFlow.getEstateDist();
         for (int i = 0; i < finalDeath; i++) {
             sRow[i] += ed.getValue(i) * 1000;
             taxableEstate[i] += sRow[i];
         }
 
         if (isSingle) {
             estateMap.put("Grantor Retained Annuity Trust", sRow);
         } else {
             estateMap.put("Grantor Retained Annuity Trust(s)", sRow);
         }
     }
 
     public void eIdit() {
         double sRow[] = new double[finalDeath + 1];
         for (int i = 0; i < finalDeath; i++) {
             sRow[i] = iditValues.getGratValue(i);
             taxableEstate[i] += iditValues.getGratValue(i);
         }
         if (isSingle) {
             estateMap.put("IDIT GRAT", sRow);
         } else {
             estateMap.put("IDIT GRATs", sRow);
         }
     }
 
     public void eLap() {
         double sRow[] = new double[finalDeath + 1];
         if (!lapList.isEmpty()) {
             Iterator<EstatePlanningToolBean> itr = lapList.iterator();
             while (itr.hasNext()) {
                 EstatePlanningToolBean lap = (EstatePlanningToolBean) itr.next();
                 LiquidAssetProtectionTool l = new LiquidAssetProtectionTool();
                 l.setId(lap.getToolId());
                 l.read();
                 l.calculate();
                 for (int i = 0; i < finalDeath; i++) {
                     sRow[i] += l.getLifeFaceValue();
                     netToFamily[i] += sRow[i];
                 }
 
             }
         }
         estateMap.put("LAP Life Insurance", sRow);
     }
 
     public void eLife() {
         double sRow[] = new double[finalDeath + 1];
 
         // Get Grat Life
         // double gLifeB = gratValues.getLifeDeathBenefit();
         // double gLifeC = gratValues.getLifeCashValue();
         // int gTerm = gratValues.maxTerm;
 
         LineObject life = gratCashFlow.getLifeInsurance();
         for (int i = 0; i < finalDeath; i++) {
             sRow[i] += life.getValue(i) * 1000;
         }
 
         // Get Idit Life
         double iLifeB = iditValues.getLifeDeathBenefit();
         double iLifeC = iditValues.getLifeCashValue();
         int iTerm = iditValues.getTerm();
 
         for (int i = 0; i < iTerm; i++) {
             sRow[i] += iLifeB;
             netToFamily[i] += iLifeB;
         }
         sRow[iTerm] += iLifeC;
 
         estateMap.put("Life Insurance From GRAT(s)", sRow);
 
     }
 
     public void eLLC() {
         double v = 2100000;
         double cash[] = new double[40];
 
         cash[0] = v;
         for (int i = 1; i < 40; i++) {
             cash[i] = v + cash[i - 1];
         }
 
         estateMap.put("LLC Note To Charity", cash);
     }
 
     public void eMGT() {
         double sRow[] = new double[finalDeath + 1];
         for (int i = 0; i < finalDeath; i++) {
             sRow[i] = mgtValues.getMgtValue(i);
             netToFamily[i] += sRow[i];
         }
         estateMap.put("Multigenerational Trusts", sRow);
     }
 
     public void eNetInflation() {
         double zRow[] = new double[finalDeath + 1];
         for (int i = 0; i < finalDeath; i++) {
             zRow[i] = (netToFamily[i] + taxableEstate[i]) * Math.pow((1 + cashFlowVars.getInflation()), -i);
         }
 
         estateMap.put("Net To Family Inflation Adjusted", zRow);
     }
 
     public void eNetToCharity() {
         // Insert the retirement plan that we want to give to charity here.
         double c1[] = (double[]) estateMap.get("Amount to Charity from CRT");
         double c2[] = (double[]) estateMap.get("Amount to Charity from CLAT");
         double c3[] = (double[]) estateMap.get("PV of AMT to Charity from Test. CLAT");
         double c5[] = (double[]) estateMap.get("Amount to Charity from TCLAT");
         for (int i = 0; i < finalDeath; i++) {
             if (useTClat) {
                 netToCharity[i] += c1[i] + c2[i] + c3[i] + c5[i] + marcCharity[i];
             } else {
                 netToCharity[i] += c1[i] + c2[i] + c5[i] + marcCharity[i];
             }
         }
 
         double amt = 0;
 
         for (int i = 0; i < finalDeath; i++) {
             //amt = charitableGift[i] + getVcfCharity(i) + cDeductions[i] + (amt * 1.06);
             amt = charitableGift[i] + getVcfCharity(i) + (amt * 1.06);
             netToCharity[i] += amt;
         }
 
         estateMap.put("TOTAL TO CHARITY", netToCharity);
     }
 
     /*
      * public void buildSalary() { double sRow[] = new double[finalDeath + 1];
      * for (int i = 0; i < finalDeath; i++) { sRow[i] = salary.getSalary(i); }
      * cashFlowMap.put("Consulting/Salary", sRow); }
      */
     public void eNetToFamily() {
 
         double zRow[] = new double[finalDeath + 1];
         for (int i = 0; i < finalDeath; i++) {
             zRow[i] = netToFamily[i] + taxableEstate[i] - abEstate[i];
             finalToFamily[i] = netToFamily[i] + taxableEstate[i] - abEstate[i];
         }
 
         estateMap.put("NET TO FAMILY", zRow);
     }
 
     public void eNetworth() {
         double nw[] = (double[]) netWMap.get("END OF YEAR NET WORTH");
         estateMap.put("Estate Net Worth", nw);
     }
 
     public void ePat() {
         double sRow[] = new double[finalDeath + 1];
         for (int i = 0; i < finalDeath; i++) {
             sRow[i] = patValues.growth[i];
         }
 
         estateMap.put("Asset from Private Annuity", sRow);
     }
 
     public void eQprt() {
         for (QprtTool qprt : qprtToolList) {
             double assetValue = qprt.getValue();
             double assetGrowth = qprt.getGrowth();
 
             int trusts = qprt.getNumberOfTrusts();
             double sRow[] = new double[finalDeath + 1];
 
             if (trusts == 2) {
                 int cTerm = qprt.getClientTerm();
                 int start = qprt.getClientStartTerm();
                 if (start <= 1) {
                     start = 0;
                 }
                 for (int i = 0; i < finalDeath; i++) {
                     if (i >= cTerm + start) {
                         sRow[i] += (assetValue / 2) * Math.pow(1 + assetGrowth, i + 1);
                     } else {
                         sRow[i] += 0;
                     }
                     netToFamily[i] += sRow[i];
                 }
                 int sTerm = qprt.getSpouseTerm();
                 start = qprt.getSpouseStartTerm();
                 if (start <= 1) {
                     start = 0;
                 }
 
                 for (int i = 0; i < finalDeath; i++) {
                     if (i >= sTerm + start) {
                         sRow[i] += (assetValue / 2) * Math.pow(1 + assetGrowth, i + 1);
                     } else {
                         sRow[i] += 0;
                     }
                     netToFamily[i] += sRow[i];
                 }
             } else {
                 int cTerm = qprt.getClientTerm();
                 int start = qprt.getClientStartTerm();
                 if (start <= 1) {
                     start = 0;
                 }
                 for (int i = 0; i < finalDeath; i++) {
                     if (i >= cTerm + start) {
                         sRow[i] += (assetValue) * Math.pow(1 + assetGrowth, i + 1);
                     } else {
                         sRow[i] += 0;
                     }
                     netToFamily[i] += sRow[i];
                 }
             }
             String desc = "QPRT  " + qprt.getAssetName();
             estateMap.put(desc, sRow);
         }
     }
 
     public void estateCharityVariables() {
         for (VariableCashFlow v : vcfItems) {
             if (v.getCfType().equals("X")) {
                 // The following is for Marc Sheridan
                 if(v.getDescription().equals("DBX")){
                     double xRow[] = (double[]) netWMap.get("Retirement Plans");
                     for(int i = 0; i < xRow.length; i++){
                         marcCharity[i] = xRow[i];
                     }
                     estateMap.put("Gift of Retirement Plan", marcCharity);
 
                 } else {
                     estateMap.put(v.getDescription(), v.getVTable());
                     double[] vt = v.getVTable();
                     for (int i = 0; i < finalDeath; i++) {
                         netToCharity[i] += vt[i];
                     }
                 }
             }
         }
     }
 
     public void estateDistribution() {
         eNetworth();
         calcABEstate(); // need to do this before tclat
         eTaxableVCF();
         eGrat();
         eIdit();
         eSidit();
         eFLP();
         eTaxableLife();
         eTClat(); // This will call eClat2();
         eABTrust();
         eTaxableEstate();
         eTaxes();
         eFlp2Family();
         eMGT();
         eQprt();
         eFamilyGrat();
         eFamilyIdit();
         eFamilySIdit();
         eFamilySplit();
         eLife();
         eWpt();
         eFamilyRPM();
         eClat();
         eLap();
         // eTClatRemainder();
         eTclatPV();
         eCrummey();
         eCrumLife();
         ePat();
         estateVariables();
         buildFamilyErp();
         eNetToFamily();
         eNetInflation();
 
         eCharityFromCrut();
         eCharityFromClat();
         eCharityFromTClat2();
         eCharityFromTClat();
         estateCharityVariables();
         eNetToCharity();
         CFRow r = new CFRow();
 
         r.setHeader("IV. Estate Distribution");
         r.setIndentLevel(0);
         r.setTextFill(0); // Left
         r.setFontSize(2);
         r.setFontWeight(Font.BOLD);
 
         rows.add(r);
 
         for (String entry : estateMap.keySet()) {
             double sRow[] = (double[]) estateMap.get(entry);
             int weight = Font.PLAIN;
             Integer w = (Integer) fontMap.get(entry);
             if (w != null) {
                 weight = w.intValue();
             }
             buildRows(entry, sRow, weight, 1);
         }
         /*
          * Set keys = estateMap.keySet(); Iterator kItr = keys.iterator();
          *
          * while (kItr.hasNext()) { String entry = (String) kItr.next(); double
          * sRow[] = (double[]) estateMap.get(entry); int weight = Font.PLAIN;
          * Integer w = (Integer) fontMap.get(entry); if (w != null) { weight =
          * w.intValue(); } buildRows(entry, sRow, weight, 1); }
          */
 
         buildErpSideFund();
 
     }
 
     public void estateVariables() {
         for (VariableCashFlow v : vcfItems) {
             if (v.getCfType().equals("F")) {
                 v.initialize();
                 estateMap.put(v.getDescription(), v.getVTable());
                 double[] vt = v.getVTable();
                 for (int i = 0; i < finalDeath; i++) {
                     netToFamily[i] += vt[i];
                 }
             }
         }
     }
 
     public void eTaxableEstate() {
 
         double xRow[] = new double[finalDeath + 1];
 
         for (int i = 0; i < finalDeath; i++) {
             xRow[i] = taxableEstate[i] - exemptionFix[i];
             if (xRow[i] < 0) {
                 xRow[i] = 0;
             }
         }
         estateMap.put("Taxable Estate", xRow);
     }
 
     public void eTaxableLife() {
         double currentCash = 0;
         double futureCash = 0;
         EstatePlanAssets epa = new EstatePlanAssets(scenarioId);
         double sRow[] = new double[finalDeath + 1];
 
         ArrayList<EPTAssetBean> bList = epa.getAssetTypes(10);
         LoadAssetBean lab = new LoadAssetBean();
         Iterator<EPTAssetBean> i = bList.iterator();
         while (i.hasNext()) {
             EPTAssetBean eab = (EPTAssetBean) i.next();
             eab.setDbObject();
             double rValue = eab.getRemainingValue();
             if (rValue > 0) {
                 InsuranceBean bb = (InsuranceBean) lab.loadAssetBean(eab);
                 double face = bb.getFaceValue();
                 currentCash = bb.getValue();
                 futureCash = bb.getFutureCashValue();
                 double v = currentCash;
                 double amt = (futureCash - currentCash) / 10.0;
                 if (amt < 0) {
                     amt = 0;
                 }
                 for (int k = 0; k < finalDeath; k++) {
                     if (bb.getTaxableLifeInc().equals("Y")) {
                         sRow[k] += face - v;
                         taxableEstate[k] += face - v;
                     } else {
                         sRow[k] += face;
                         taxableEstate[k] += face;
                     }
                     v += amt;
                 }
             }
         }
         estateMap.put("Taxable Life Insurance", sRow);
     }
 
     public void eTaxableVCF() {
         for (VariableCashFlow v : vcfItems) {
             if (v.getCfType().equals("E")) {
                 // The following is for the defined benifit disbursement for Marc Sheridan
                 if(v.getDescription().equals("DBX")){
                     double xRow[] = (double[]) netWMap.get("Retirement Plans");
                     double vRow[] = new double[xRow.length];
                     for(int i=0; i < xRow.length; i++){
                         vRow[i] = xRow[i] * -1;
                         taxableEstate[i] += vRow[i];
                     }
                     estateMap.put("Balance of Retirement", xRow);
 
                 } else {
                     estateMap.put(v.getDescription(), v.getVTable());
                     double[] vt = v.getVTable();
                     for (int i = 0; i < finalDeath; i++) {
                         taxableEstate[i] += vt[i];
                     }
                 }
             }
         }
     }
 
     public void eTaxes() {
         double sRow[] = new double[finalDeath + 1];
         double eTax[] = new double[finalDeath + 1];
         double fTax[] = new double[finalDeath + 1];
         double sTax[] = new double[finalDeath + 1];
         CalcEstateTax cet = new CalcEstateTax();
         Date now = new Date();
         Calendar cal = Calendar.getInstance();
         cal.setTime(now);
         int year = cal.get(Calendar.YEAR);
         double r[] = retirementCashFlow.getTotalValue();
 
         for (int i = 0; i < finalDeath; i++) {
             // zcalc already add in the first exemption, but we want to show the full in
             // the report, so we add in the second if it exists to get the correct Federal Estate Tax.
             cet.setTaxableEstate(taxableEstate[i]);
             cet.setYear(year);
             cet.setRpBalance(rpmValues.getValue(i) + r[i]);
             cet.setClientTaxableGifts(getClientGiftTax(i));
             cet.setSpouseTaxableGifts(getSpouseGiftTax(i));
             cet.setStateTaxRate(cashFlowVars.getStateTaxRate());
             cet.calculateTax();
             year++;
             eTax[i] = cet.getEstateTax();
             fTax[i] = cet.getFederalIncomeTax();
             sTax[i] = cet.getStateTax();
             sRow[i] = cet.getTotalIncomeTax();
             netToFamily[i] -= sRow[i];
         }
         if (isSingle) {
             estateMap.put("   Federal Estate Tax", eTax);
             estateMap.put("   Federal Income Tax", fTax);
             estateMap.put("   State Income Tax", sTax);
             estateMap.put("TOTAL TAXES AT DEATH", sRow);
         } else {
             estateMap.put("   Federal Estate Tax", eTax);
             estateMap.put("   Federal Income Tax", fTax);
             estateMap.put("   State Income Tax", sTax);
             estateMap.put("TOTAL TAXES AT 2ND DEATH", sRow);
         }
 
     }
 
     public void eTClat() {
         tclat = new double[finalDeath];
         tclatRI = new double[finalDeath];
         tclatCharity = new double[finalDeath];
         eTClat2();
         if (!tClatList.isEmpty()) {
             useTClat = true;
             int term = 10;
             Iterator<EstatePlanningToolBean> itr = tClatList.iterator();
             double nw[] = (double[]) netWMap.get("END OF YEAR NET WORTH");
             double r[] = (double[]) retirementCashFlow.getTotalValue();
             while (itr.hasNext()) {
                 EstatePlanningToolBean t = (EstatePlanningToolBean) itr.next();
                 TClatTool tool = new TClatTool();
                 tool.setId(t.getToolId());
                 tool.setDbObject();
                 tool.read();
                 tool.setScenarioId(scenarioId);
                 for (int i = 0; i < finalDeath; i++) {
                     // use the following for Dave Durow.
                     // tool.stdCalc(nw[i]- (r[i]),-(abEstate[i]));
                     //tool.stdCalc(nw[i] - (eNetR[i]), -(abEstate[i]));
                     /**
                      * TODO - We need to allow the desinger or planner to specify a Remainder Interest
                      * for the TCLAT. This will allow the planner to be much more specific.
                      * For now we will use 50,000 so that it can be sold to the MGEN trust.
                      * -Paul April 2009
                      */
                     //tool.stdCalc(nw[i], 50000);
                     tool.stdCalc(taxableEstate[i], -abEstate[i]);
                     tclat[i] = -tool.getClatDeduction();
                     tclatRI[i] = tool.getRemainderInterest();
                     tclatCharity[i] = tool.getAnnuity() * tool.getTerm();
                     term = tool.getTerm();
                     taxableEstate[i] += tclat[i] + (r[i] - eNetR[i]) / 1000;
                 }
             }
             estateMap.put("Asssets to " + Integer.toString(term) + " Yr Test. Clat", tclat);
         }
     }
 
     public void eTClat2() {
         if (tClat2List.isEmpty()) {
             return;
         }
         double sRow[] = new double[finalDeath + 1];
 
         Iterator<EstatePlanningToolBean> itr = tClat2List.iterator();
 
         while (itr.hasNext()) {
             useTClat2 = true;
             EstatePlanningToolBean cr = (EstatePlanningToolBean) itr.next();
             TClat2 tclat = new TClat2();
             tclat.setId(cr.getToolId());
             tclat.read();
             tclat.calculate();
             for (int i = 0; i < finalDeath; i++) {
                 sRow[i] -= tclat.getClatDeduction();
                 taxableEstate[i] -= tclat.getClatDeduction();
                 // tclat2Charity[i] += (tclat.getAmount() -
                 // tclat.getRemainderInterest()) * 1.03;
                 tclat2Charity[i] += tclat.getAnnuity() * tclat.getTerm();
                 tclat2Family[i] += tclat.getClatDeduction();
             }
         }
 
         estateMap.put("Charitable Deduction from assets placed in TClat", sRow);
     }
 
     // This is a bit different in that we need to calculate and initialize it
     // all here.
     public void eTClatOld() {
         if (tClatList.isEmpty()) {
             useTClat = false;
             double nw[] = (double[]) netWMap.get("END OF YEAR NET WORTH");
             estateMap.put("Estate Net Worth", nw);
         }
         if (useTClat) {
             tclatValues = new TClatValues();
             tclatValues.scenarioId = scenarioId;
             tclatValues.finalDeath = finalDeath;
             tclatValues.estate = estate;
             Iterator<EstatePlanningToolBean> itr = tClatList.iterator();
             while (itr.hasNext()) {
                 EstatePlanningToolBean tclat = (EstatePlanningToolBean) itr.next();
                 tclatValues.calculateTool(tclat.getToolId());
             }
 
             if (tclatValues.inclusive.equals("Y")) {
                 double nw[] = (double[]) netWMap.get("END OF YEAR NET WORTH");
 
                 for (int i = 0; i < finalDeath; i++) {
                     double value = nw[i];// - gpAssets.getGpValue(i);
                     tclatValues.setTClatValue(value, i);
                 }
             }
             tclatValues.calculate();
 
             // OK now we build the tclat tables
             double sRow[] = new double[finalDeath + 1];
             double zRow[] = new double[finalDeath + 1];
             for (int i = 0; i < finalDeath; i++) {
                 sRow[i] = tclatValues.getTClatValue(i);
                 zRow[i] = -tclatValues.getTClatValue(i);
                 taxableEstate[i] += zRow[i];
             }
 
             estateMap.put("Assets Subject to Tax", sRow);
             estateMap.put("Assets to Test-Clat " + tclatValues.term + " Yr Test. CLAT", zRow);
         }
     }
 
     public void eTclatPV() {
         if (useTClat) {
             double sRow[] = new double[finalDeath + 1];
             for (int i = 0; i < finalDeath; i++) {
                 sRow[i] = -tclat[i] - tclatRI[i];
 
                 netToFamily[i] += sRow[i];
             }
             estateMap.put("Present Value Test. CLAT", sRow);
         }
         if (useTClat2) {
             double fRow[] = new double[finalDeath + 1];
             for (int i = 0; i < finalDeath; i++) {
                 fRow[i] = tclat2Family[i];
             }
             estateMap.put("Test. CLAT to Family", fRow);
         }
     }
 
     public void eTClatRemainder() {
         if (useTClat) {
             double sRow[] = new double[finalDeath + 1];
             for (int i = 0; i < finalDeath; i++) {
                 sRow[i] = -tclatValues.remainderInterest[i];
                 netToFamily[i] += sRow[i];
             }
             estateMap.put("Test. CLAT Remainder Interest", sRow);
         }
     }
 
     public void eWpt() {
         double sRow[] = new double[finalDeath + 1];
         double db = wptValues.getDeathBenefit();
         for (int i = 0; i < finalDeath; i++) {
             sRow[i] = db;
             netToFamily[i] += db;
         }
 
         estateMap.put("Life Insurance from CRUT", sRow);
 
     }
 
     public void genChartData() {
         double fam[] = (double[]) estateMap.get("NET TO FAMILY");
         double eTax[];
         if (isSingle) {
             eTax = (double[]) estateMap.get("TOTAL TAXES AT DEATH");
         } else {
             eTax = (double[]) estateMap.get("TOTAL TAXES AT 2ND DEATH");
         }
         double c4[] = (double[]) estateMap.get("TOTAL TO CHARITY");
 
         for (int i = 0; i < finalDeath; i++) {
             chartTax[i] = eTax[i];
             if (useTClat) {
                 chartSplit[i] = taxableEstate[i] - eTax[i] - tclatRI[i];
             } else {
                 chartSplit[i] = taxableEstate[i] - eTax[i];
             }
             chartCharity[i] = c4[i];
             chartFamily[i] = finalToFamily[i];
             chartEstate[i] = taxableEstate[i];
         }
 
     }
 
     /**
      * @return Returns the chartCharity.
      */
     public double[] getChartCharity() {
         return chartCharity;
     }
 
     /**
      * @return Returns the chartEstate.
      */
     public double[] getChartEstate() {
         return chartEstate;
     }
 
     /**
      * @return Returns the chartFamily.
      */
     public double[] getChartFamily() {
         return chartFamily;
     }
 
     /**
      * @return Returns the chartSplit.
      */
     public double[] getChartSplit() {
         return chartSplit;
     }
 
     /**
      * @return Returns the chartTax.
      */
     public double[] getChartTax() {
         return chartTax;
     }
 
     public double getClientGiftTax(int year) {
         clientGiftTax.finalDeath = finalDeath;
         clientGiftTax.ownerId = clientBean.getPrimaryId();
         clientGiftTax.crumGift = (crummValues.getAnnualGift()) / 2.0;
         clientGiftTax.crumTerm = crummValues.getTerm();
         clientGiftTax.init();
         double giftValue = 0;
 
         // WQe need to add the qprt gifts here
         for (QprtTool qprt : qprtToolList) {
             giftValue += qprt.getClientPriorGifts();
         }
 
         if (year == 0) {
             giftValue = gratCashFlow.getTaxableGift() / 2 + rpmValues.getToTrust(0) / 2 + mgtValues.getAssetValue() / 2;
         } else {
             giftValue += rpmValues.getToTrust(year) / 2;
         }
 
         return giftValue;
     }
 
     public double getCRTotal(int year) {
 
         Iterator<String> key = cashFlowMap.keySet().iterator();
         double total = 0.0;
         while (key.hasNext()) {
             String k = (String) key.next();
             double[] rMap = (double[]) cashFlowMap.get(k);
             total += rMap[year];
         }
         for (VariableCashFlow v : vcfItems) {
             if (v.getCfType().equalsIgnoreCase("C")) {
                 double sRow[] = v.getVTable();
                 total += sRow[year];
             }
         }
         return total;
     }
 
     /*
      * public void buildLiving() { double sRow[] = new double[finalDeath+1]; for
      * (int i = 0; i < finalDeath; i++) { sRow[i] = livingExpense.getLiving(i);
      * } dMap.put("Living Expenses", sRow); }
      */
     public double getDisTotal(int year) {
         Iterator<String> i = dMap.keySet().iterator();
         double total = 0.0;
         while (i.hasNext()) {
             double[] cMap = (double[]) dMap.get(i.next());
             total += cMap[year];
         }
         for (VariableCashFlow v : vcfItems) {
             if (v.getCfType().equalsIgnoreCase("D")) {
                 double sRow[] = v.getVTable();
                 total += sRow[year];
             }
         }
 
         return total;
     }
 
     public double getNetTotal(int year) {
         Iterator<String> i = netWMap.keySet().iterator();
         double total = 0.0;
         while (i.hasNext()) {
             double[] cMap = (double[]) netWMap.get(i.next());
             double v = Math.rint(cMap[year] / 1000.0);
             total += (v * 1000.0);
         }
 
         return total;
     }
 
     public double getSpouseGiftTax(int year) {
         double giftValue = 0;
         spouseGiftTax.finalDeath = finalDeath;
         spouseGiftTax.ownerId = clientBean.getPrimaryId();
         spouseGiftTax.crumGift = (crummValues.getAnnualGift()) / 2.0;
         spouseGiftTax.crumTerm = crummValues.getTerm();
         spouseGiftTax.init();
         for (QprtTool qprt : qprtToolList) {
             giftValue = qprt.getSpousePriorGifts();
         }
         if (year == 0) {
             giftValue += gratCashFlow.getTaxableGift() / 2 + rpmValues.getToTrust(0) / 2 + mgtValues.getAssetValue() / 2;
         } else {
             giftValue += rpmValues.getToTrust(year) / 2;
         }
 
         return giftValue;
     }
 
     public double getVcfCharity(int year) {
         double total = 0.0;
         for (VariableCashFlow v : vcfItems) {
             if (v.getCdType().equals("Y")) {
                 total += v.getVTable()[year];
             }
         }
         return total;
     }
 
     public double getVcfTax(int year) {
         double total = 0.0;
         for (VariableCashFlow v : vcfItems) {
             total += v.getTaxableValue(year);
         }
         return total;
     }
 
     public void initClatValues() {
         clatValues = new ClatValues();
         clatValues.finalDeath = finalDeath;
         clatValues.setEstate(estate);
 
         Iterator<EstatePlanningToolBean> itr = clatList.iterator();
         while (itr.hasNext()) {
             EstatePlanningToolBean clat = (EstatePlanningToolBean) itr.next();
             clatValues.calculate(clat.getToolId());
         }
     }
 
     public void initColorMap() {
         colorMap.put("NET TO FAMILY", Color.GREEN);
         colorMap.put("   Federal Income Tax", Color.RED);
         colorMap.put("   State Income Tax", Color.RED);
         colorMap.put("   Federal Estate Tax", Color.RED);
         colorMap.put("TOTAL TAXES AT 2ND DEATH", Color.RED);
         colorMap.put("TOTAL TAXES AT DEATH", Color.RED);
         colorMap.put("PV of AMT to Charity from Test. CLAT", Color.BLUE);
         colorMap.put("Amount to Charity from CRT", Color.BLUE);
         colorMap.put("Amount to Charity from CLAT", Color.BLUE);
         colorMap.put("TOTAL TO CHARITY", Color.BLUE);
         colorMap.put("END OF YEAR NET WORTH", Color.BLUE);
         colorMap.put("Net To Family Inflation Adjusted", Color.GREEN);
         colorMap.put("LLC Note To Charity", Color.BLUE);
         colorMap.put("Life Insurance", Color.BLUE);
         colorMap.put("Amount to Charity from TCLAT", Color.BLUE);
     }
 
     public void initCrummValues() {
         crummValues = new CrummeyValues();
         crummValues.initialize();
         Iterator<EstatePlanningToolBean> itr = crumList.iterator();
         while (itr.hasNext()) {
             EstatePlanningToolBean cr = (EstatePlanningToolBean) itr.next();
             crummValues.calculate(cr.getToolId());
         }
     }
 
     public void initErp() {
         cplanList = new ArrayList<CharliePlanTool>();
         Iterator<EstatePlanningToolBean> itr = erpList.iterator();
         while (itr.hasNext()) {
             EstatePlanningToolBean ep = (EstatePlanningToolBean) itr.next();
             CharliePlanTool cp = new CharliePlanTool();
             cp.setId(ep.getToolId());
             cp.read();
             cp.setClientAge(clientAge);
             cp.setSpouseAge(spouseAge);
             cp.setFinalDeath(finalDeath);
             cp.calculate();
             cplanList.add(cp);
         }
     }
 
     public void initFontMap() {
         fontMap.put("Taxable Estate", new Integer(Font.BOLD));
         fontMap.put("NET TO FAMILY", new Integer(Font.BOLD));
         fontMap.put("TOTAL TO CHARITY", new Integer(Font.BOLD));
         fontMap.put("TOTAL TAXES AT 2ND DEATH", new Integer(Font.BOLD));
 
     }
 
     public void initGratValues() {
 
         gratCashFlow = new GratCashFlow2();
         Iterator<EstatePlanningToolBean> itr = gratList.iterator();
         while (itr.hasNext()) {
             EstatePlanningToolBean grat = (EstatePlanningToolBean) itr.next();
             gratCashFlow.setGrat((int) grat.getToolId());
         }
     }
 
     public void initialize() {
         initColorMap();
         initFontMap();
 
         retirementAge = TeagDefines.RETIREMENT_AGE;
 
         client.setId(clientBean.getPrimaryId());
         client.setDbObject();
         client.initialize();
 
         Marriages m = new Marriages();
         m.setPrimaryId(client.getId());
         m.initialize();
         MarriageBean mb = m.getCurrentMarriage();
 
         if (mb == null) {
             isSingle = true;
         }
 
         if (!isSingle) {
             spouse.setId(mb.getSpouseId());
             spouse.setDbObject();
             spouse.initialize();
             spouseAge = Utilities.CalcAge(spouse.getBirthDate());
         }
         clientAge = Utilities.CalcAge(client.getBirthDate());
 
         initVCF();
 
         // Get the tools for cash flow....
         et.setScenarioId(scenarioId);
         et.initialize();
         gratList = et.getToolsByType(ToolTableTypes.GRAT.id());
         wptList = et.getToolsByType(ToolTableTypes.CRT.id());
         rpmList = et.getToolsByType(ToolTableTypes.RPM.id());
         iditList = et.getToolsByType(ToolTableTypes.IDIT.id());
         gpList = et.getToolsByType(ToolTableTypes.FLP.id());
         crumList = et.getToolsByType(ToolTableTypes.CRUM.id());
         qprtList = et.getToolsByType(ToolTableTypes.QPRT.id());
         mgtList = et.getToolsByType(ToolTableTypes.MGEN.id());
         clatList = et.getToolsByType(ToolTableTypes.CLAT.id());
         tClatList = et.getToolsByType(ToolTableTypes.TCLAT.id());
         tClat2List = et.getToolsByType(ToolTableTypes.TCLAT2.id());
         lifeList = et.getToolsByType(ToolTableTypes.LIFE.id());
         lapList = et.getToolsByType(ToolTableTypes.LAP.id());
         patList = et.getToolsByType(ToolTableTypes.PANNUITY.id());
         erpList = et.getToolsByType(ToolTableTypes.CPLAN.id());
         scinList = et.getToolsByType(ToolTableTypes.SCIN.id());
         siditList = et.getToolsByType(ToolTableTypes.SIDIT.id());
         splitList = et.getToolsByType(ToolTableTypes.SPLIT.id());
 
         socialSecurity.socialPayment = cashFlowVars.getSocialSecurity();
         socialSecurity.socialGrowth = cashFlowVars.getSocialSecurityGrowth();
         socialSecurity.calculate(clientAge, spouseAge);
 
         interestOnCash.setScenarioId(scenarioId);
         interestOnCash.finalDeath = finalDeath;
         interestOnCash.calculate();
 
         securities.setScenarioId(scenarioId);
         securities.finalDeath = finalDeath;
         securities.init();
 
         municipal.setClient(clientBean);
         municipal.setEstate(estate);
         municipal.setScenarioId(scenarioId);
         municipal.finalDeath = finalDeath;
         municipal.init();
 
         gpAssets.setScenarioId(scenarioId);
         gpAssets.calculate();
 
         lpAssets.setScenarioId(scenarioId);
         lpAssets.calculate();
 
         llcAssets.setScenarioId(scenarioId);
         llcAssets.calculate();
 
         initRetirement();
         initErp();
         initSidit();
         initScin();
         initGratValues();
         initIditGratValues();
         initWptValues();
         initRpmValues();
         initCrummValues();
         initQprtValues();
         initMgtValues();
         initLifeValues();
         initMortgage();
         initClatValues();
         initPatValues();
         initSplitValues();
 
         for (int i = 0; i < finalDeath; i++) {
             netToFamily[i] = 0;
         }
 
         cashFlow();
         netWorth();
         estateDistribution();
         genChartData();
     }
 
     public void initIditGratValues() {
         iditValues = new IditValues();
         iditValues.finalDeath = finalDeath;
         iditValues.setEstate(estate);
         iditValues.initialize();
         Iterator<EstatePlanningToolBean> itr = iditList.iterator();
         while (itr.hasNext()) {
             EstatePlanningToolBean idit = (EstatePlanningToolBean) itr.next();
             iditValues.calculate(idit.getToolId());
         }
 
     }
 
     public void initLifeValues() {
         lifeValues = new LifeToolValues();
         lifeValues.finalDeath = finalDeath;
         Iterator<EstatePlanningToolBean> itr = lifeList.iterator();
         while (itr.hasNext()) {
             EstatePlanningToolBean cr = (EstatePlanningToolBean) itr.next();
             lifeValues.calculate(cr.getToolId());
         }
     }
 
     public void initMgtValues() {
         mgtValues = new MgtValues();
         mgtValues.setScenarioId(scenarioId);
         mgtValues.setEstate(estate);
         mgtValues.finalDeath = finalDeath;
         Iterator<EstatePlanningToolBean> itr = mgtList.iterator();
         while (itr.hasNext()) {
             EstatePlanningToolBean cr = (EstatePlanningToolBean) itr.next();
             mgtValues.calculate(cr.getToolId());
         }
     }
 
     public void initMortgage() {
         mortValues = new Mortgage();
         mortValues.finalDeath = finalDeath;
         mortValues.ownerId = clientBean.getPrimaryId();
         mortValues.calculate();
     }
 
     public void initPatValues() {
         patValues = new PrivateAnnuityValues();
         patValues.cAge = clientAge;
         patValues.sAge = spouseAge;
         Iterator<EstatePlanningToolBean> itr = patList.iterator();
         while (itr.hasNext()) {
             EstatePlanningToolBean ept = (EstatePlanningToolBean) itr.next();
             patValues.calculate(ept.getToolId());
         }
     }
 
     public void initSplitValues() {
         if (!splitList.isEmpty()) {
             splitValues = new SplitDollarCashFlow();
             splitValues.processSplitDollar();
         }
     }
 
     // We need to handle all of the Qprts here. For now we are assuming that you only have one.
     public void initQprtValues() {
         qprtToolList = new ArrayList<QprtTool>();
 
         for (EstatePlanningToolBean q : qprtList) {
             QprtTool qprt = new QprtTool();
             qprt.setId(q.getToolId());
             qprt.setClientLifeExp(estate.getClientLifeExp());
             qprt.setSpouseLifeExp(estate.getSpouseLifeExp());
             qprt.setClientAge(estate.getClientAge());
             qprt.setSpouseAge(estate.getSpouseAge());
             qprt.read();
             qprt.calculate();
             qprtToolList.add(qprt);
         }
     }
 
     public void initRetirement() {
         retirementCashFlow.setSingle(isSingle);
         retirementCashFlow.setClientAge(clientAge);
         if (!isSingle) {
             retirementCashFlow.setSpouseAge(spouseAge);
         }
 
         EstatePlanAssets epa = new EstatePlanAssets(scenarioId);
 
         ArrayList<EPTAssetBean> bList = epa.getAssetTypes(6);
 
         LoadAssetBean lab = new LoadAssetBean();
         Iterator<EPTAssetBean> itr = bList.iterator();
         while (itr.hasNext()) {
             EPTAssetBean eab = (EPTAssetBean) itr.next();
             eab.setDbObject();
             double rValue = eab.getRemainingValue();
             if (rValue > 0) {
                 AssetSqlBean asb = lab.loadAssetBean(eab);
                 double remainingValue = asb.getAssetValue();
                 double growthRate = asb.getAssetGrowth();
                 retirementCashFlow.addPlan(remainingValue, growthRate);
             }
         }
     }
 
     public void initRpmValues() {
         rpmValues = new RPMValues();
         rpmValues.finalDeath = finalDeath;
         rpmValues.setEstate(estate);
         rpmValues.initialize();
         Iterator<EstatePlanningToolBean> itr = rpmList.iterator();
         while (itr.hasNext()) {
             EstatePlanningToolBean wpt = (EstatePlanningToolBean) itr.next();
             rpmValues.calculate(wpt.getToolId());
         }
     }
 
     public void initScin() {
         scinArray = new ArrayList<SCINTool>();
         Iterator<EstatePlanningToolBean> itr = scinList.iterator();
         while (itr.hasNext()) {
             EstatePlanningToolBean ep = (EstatePlanningToolBean) itr.next();
             SCINTool scin = new SCINTool();
             scin.setId(ep.getToolId());
             scin.read();
             scin.setCAge(clientAge);
             scin.setFinalDeath(finalDeath);
             scin.calculate();
             scinArray.add(scin);
         }
     }
 
     public void initSidit() {
         siditArray = new ArrayList<SIditTool>();
         Iterator<EstatePlanningToolBean> itr = siditList.iterator();
         while (itr.hasNext()) {
             EstatePlanningToolBean ep = (EstatePlanningToolBean) itr.next();
             SIditTool st = new SIditTool();
             st.setId(ep.getToolId());
             st.read();
             st.setFinalDeath(finalDeath);
             st.calculate();
             siditArray.add(st);
         }
     }
 
     public void initVCF() {
         // need to get the new line items from the database.
         VCFBean vcfBean = new VCFBean();
         VCFBean vcf[] = vcfBean.query(VCFBean.OWNER_ID, "" + client.getId());
         if (vcf != null) {
             for (int i = 0; i < vcf.length; i++) {
                 if (vcf[i].getUseFlag().equalsIgnoreCase("2") || vcf[i].getUseFlag().equalsIgnoreCase("B")) {
                     VariableCashFlow cf = new VariableCashFlow();
                     cf.setValues(vcf[i]);
                     vcfItems.add(cf);
                 }
             }
         }
     }
 
     public void initWptValues() {
         wptValues = new WPTValues();
         wptValues.finalDeath = finalDeath;
         wptValues.setEstate(estate);
         wptValues.initialize();
         Iterator<EstatePlanningToolBean> itr = wptList.iterator();
         while (itr.hasNext()) {
             EstatePlanningToolBean wpt = (EstatePlanningToolBean) itr.next();
             wptValues.calculate(wpt.getToolId());
         }
     }
 
     public void nBond() {
         double sRow[] = new double[finalDeath + 1];
         for (int i = 0; i < finalDeath; i++) {
             sRow[i] = municipal.getEndBalance(i);
         }
         netWMap.put("Muni Bonds", sRow);
     }
 
     public void nBusiness() {
         // OK first get all of the assets
         EstatePlanAssets epa = new EstatePlanAssets(scenarioId);
         ArrayList<EPTAssetBean> bList = epa.getAssetTypes(2);
 
         LoadAssetBean lab = new LoadAssetBean();
 
         Iterator<EPTAssetBean> i = bList.iterator();
         while (i.hasNext()) {
             EPTAssetBean eab = (EPTAssetBean) i.next();
             eab.setDbObject();
 
             double rValue = eab.getRemainingValue();
             if (rValue > 0) {
                 BusinessBean biz = (BusinessBean) lab.loadAssetBean(eab);
                 double iGrowth = biz.getGrowthRate();
                 if (biz.getAld() != null) {
                     SimpleDateFormat df = new SimpleDateFormat("M/d/yyyy");
                     try {
                         Date ld = df.parse(biz.getAld());
                         Calendar cal = Calendar.getInstance();
                         cal.setTime(ld);
                         Calendar cYear = Calendar.getInstance();
                         cYear.setTime(new Date());
                         int aYear = cal.get(Calendar.YEAR) - cYear.get(Calendar.YEAR);
                         double v = rValue;
                         double vtable[] = new double[finalDeath + 1];
                         for (int j = 0; j < aYear; j++) {
                             vtable[j] = v;
                             v += v * iGrowth;
                         }
                         netWMap.put(biz.getDescription(), vtable);
                     } catch (Exception e) {
                         processNetWorthAsset(eab.getDescription(), rValue,
                                 iGrowth, false);
                     }
                 } else {
                     processNetWorthAsset(eab.getDescription(), rValue, iGrowth,
                             false);
                 }
             }
         }
     }
 
     public void nCash() {
         double sRow[] = new double[finalDeath + 1];
         for (int i = 0; i < finalDeath; i++) {
             sRow[i] = interestOnCash.getCashValue(i);
         }
         netWMap.put("Checking", sRow);
     }
 
     public void nDebt() {
         // OK first get all of the assets
         EstatePlanAssets epa = new EstatePlanAssets(scenarioId);
         ArrayList<EPTAssetBean> bList = epa.getAssetTypes(4); // 8 = Real Estate
 
         LoadAssetBean lab = new LoadAssetBean();
 
         Iterator<EPTAssetBean> i = bList.iterator();
         while (i.hasNext()) {
             EPTAssetBean eab = (EPTAssetBean) i.next();
             eab.setDbObject();
             double rValue = eab.getRemainingValue();
             if (rValue > 0) {
                 AssetSqlBean asb = lab.loadAssetBean(eab);
                 BusinessBean bb = (BusinessBean) lab.loadAssetBean(eab);
                 int aYear = finalDeath;
                 if (bb.getAld() != null) {
                     SimpleDateFormat df = new SimpleDateFormat("M/d/yyyy");
                     try {
                         Calendar cal = Calendar.getInstance();
                         Date now = new Date();
                         cal.setTime(now);
                         int currentYear = cal.get(Calendar.YEAR);
                         Date ald = df.parse(bb.getAld());
                         cal.setTime(ald);
                         aYear = cal.get(Calendar.YEAR) - currentYear;
                         double sRow[] = new double[finalDeath];
                         double netValue = rValue;
                         for (int j = 0; j < aYear; j++) {
                             netValue += netValue * bb.getGrowthRate();
                             sRow[j] = netValue;
                         }
                         netWMap.put(bb.getDescription(), sRow);
                     } catch (Exception e) {
                     }
                 }
                 if (aYear == finalDeath) {
                     double interestRate = asb.getAssetIncome();
                     processNetWorthAsset(eab.getDescription(), rValue,
                             interestRate, false);
                 }
             }
         }
     }
 
     public void netWorth() {
         nCash();
         nBond();
         nSecurities();
         nRetire();
         nBusiness();
         nIlliquid();
         nLLC();
         nRealEstate();
         nPersonal();
         nQprt();
         nGP();
         nLP();
         nLife();
         nSidit();
         nScin();
         nGrat();
         nSplit();
 
         networthVariables();
 
         nMortgage();
         nPersonalDebt();
         // nDebt();
         buildNetWorthNotes();
         buildNetWorthNotesPayable();
         buildNetTotal();
 
         CFRow r = new CFRow();
         r.setHeader("III. Net Worth");
         r.setIndentLevel(0);
         r.setTextFill(0); // Left
         r.setFontSize(2);
         r.setFontWeight(Font.BOLD);
 
         rows.add(r);
 
         for (String entry : netWMap.keySet()) {
             double sRow[] = (double[]) netWMap.get(entry);
             buildRows(entry, sRow, Font.PLAIN, 1);
         }
 
         /*
          * Set keys = netWMap.keySet(); Iterator kItr = keys.iterator();
          *
          * while (kItr.hasNext()) { String entry = (String) kItr.next(); double
          * sRow[] = (double[]) netWMap.get(entry); buildRows(entry, sRow,
          * Font.PLAIN, 1); }
          */
     }
 
     public void networthVariables() {
         for (VariableCashFlow v : vcfItems) {
             if (v.getCfType().equals("N")) {
                 if (netWMap.containsKey(v.getDescription())) {
                     double vt[] = v.getVTable();
                     double nv[] = (double[]) netWMap.get(v.getDescription());
                     for (int i = 0; i < vt.length; i++) {
                         nv[i] += vt[i];
                     }
                     netWMap.put(v.getDescription(), nv);
                 } else {
                     netWMap.put(v.getDescription(), v.getVTable());
                 }
             }
         }
 
     }
 
     public void nFLP() {
         EstatePlanAssets epa = new EstatePlanAssets(scenarioId);
         ArrayList<EPTAssetBean> bList = epa.getAssetTypes(12); // FLP GP
 
         LoadAssetBean lab = new LoadAssetBean();
 
         Iterator<EPTAssetBean> i = bList.iterator();
         while (i.hasNext()) {
             EPTAssetBean eab = (EPTAssetBean) i.next();
             eab.setDbObject();
             double rValue = eab.getRemainingValue();
             if (rValue > 0) {
                 AssetSqlBean asb = lab.loadAssetBean(eab);
                 double iGrowth = asb.getAssetGrowth();
                 processNetWorthAsset(eab.getDescription(), rValue, iGrowth,
                         false);
             }
         }
 
         bList = epa.getAssetTypes(13); // FLP LP
         i = bList.iterator();
 
         while (i.hasNext()) {
             EPTAssetBean eab = (EPTAssetBean) i.next();
             eab.setDbObject();
             double rValue = eab.getRemainingValue();
             if (rValue > 0) {
                 AssetSqlBean asb = lab.loadAssetBean(eab);
                 double iGrowth = asb.getAssetGrowth();
                 processNetWorthAsset(eab.getDescription(), rValue, iGrowth,
                         false);
             }
         }
     }
 
     public void nGP() {
         double sRow[] = new double[finalDeath + 1];
         for (int i = 0; i < finalDeath; i++) {
             sRow[i] = gpAssets.getGpValue(i);
         }
         netWMap.put("General Partner Retained Interest in FLP", sRow);
     }
 
     public void nSplit() {
         if (!splitList.isEmpty()) {

            double bNet[] = splitValues.getBondNetworth();
            double tRow[] = new double[finalDeath + 1];
            for(int i=0; i < finalDeath; i++){
                tRow[i] = bNet[i];
            }
            netWMap.put("Split Bonds", tRow);

             double net[] = splitValues.getNetworth();
             double sRow[] = new double[finalDeath + 1];
             for (int i = 0; i < finalDeath; i++) {
                 sRow[i] = net[i];
             }
 
             netWMap.put("Split Dollar Trust Loan", sRow);
         }
     }
 
     // If a grat does not start in the first year we need to add in the asset
     // values until it does as
     // the assets are still in the estate!
     public void nGrat() {
         ArrayList<LineObject> assets = gratCashFlow.getAssetNetWorth();
         for (LineObject l : assets) {
             double sRow[] = new double[finalDeath + 1];
             for (int i = 0; i < finalDeath; i++) {
                 sRow[i] = l.getValue(i) * 1000.0;
             }
             netWMap.put(l.getDescription(), sRow);
         }
     }
 
     public void nIlliquid() {
         // OK first get all of the assets
         EstatePlanAssets epa = new EstatePlanAssets(scenarioId);
         ArrayList<EPTAssetBean> bList = epa.getAssetTypes(5); // 5 = Illiquid
         // assets
 
         LoadAssetBean lab = new LoadAssetBean();
 
         Iterator<EPTAssetBean> i = bList.iterator();
         while (i.hasNext()) {
             EPTAssetBean eab = (EPTAssetBean) i.next();
             eab.setDbObject();
             double rValue = eab.getRemainingValue();
             if (rValue > 0) {
                 AssetSqlBean asb = lab.loadAssetBean(eab);
                 double iGrowth = asb.getAssetGrowth();
                 processNetWorthAsset(eab.getDescription(), rValue, iGrowth,
                         false);
             }
         }
     }
 
     public void nLife() {
         double currentCash = 0;
         double futureCash = 0;
         EstatePlanAssets epa = new EstatePlanAssets(scenarioId);
         double sRow[] = new double[finalDeath + 1];
 
         ArrayList<EPTAssetBean> bList = epa.getAssetTypes(10);
         LoadAssetBean lab = new LoadAssetBean();
         Iterator<EPTAssetBean> i = bList.iterator();
         while (i.hasNext()) {
             EPTAssetBean eab = (EPTAssetBean) i.next();
             eab.setDbObject();
             double rValue = eab.getRemainingValue();
             if (rValue > 0) {
                 InsuranceBean bb = (InsuranceBean) lab.loadAssetBean(eab);
                 currentCash = bb.getValue();
                 futureCash = bb.getFutureCashValue();
                 double v = currentCash;
                 double amt = (futureCash - currentCash) / 10.0;
                 if (amt < 0) {
                     amt = 0;
                 }
                 for (int j = 0; j < finalDeath; j++) {
                     sRow[j] += v;
                     v += amt;
                 }
             }
         }
 
         netWMap.put("Life Insurance", sRow);
     }
 
     public void nLLC() {
         EstatePlanAssets epa = new EstatePlanAssets(scenarioId);
         ArrayList<EPTAssetBean> bList = epa.getAssetTypes(14); // LLC MMI
 
         LoadAssetBean lab = new LoadAssetBean();
 
         Iterator<EPTAssetBean> i = bList.iterator();
         while (i.hasNext()) {
             EPTAssetBean eab = (EPTAssetBean) i.next();
             eab.setDbObject();
             double rValue = eab.getRemainingValue();
             if (rValue > 0) {
                 AssetSqlBean asb = lab.loadAssetBean(eab);
                 double iGrowth = asb.getAssetGrowth();
                 processNetWorthAsset(eab.getDescription(), rValue, iGrowth,
                         false);
             }
         }
 
         bList = epa.getAssetTypes(15); // LLC MI
         i = bList.iterator();
 
         while (i.hasNext()) {
             EPTAssetBean eab = (EPTAssetBean) i.next();
             eab.setDbObject();
             double rValue = eab.getRemainingValue();
             if (rValue > 0) {
                 AssetSqlBean asb = lab.loadAssetBean(eab);
                 double iGrowth = asb.getAssetGrowth();
                 processNetWorthAsset(eab.getDescription(), rValue, iGrowth,
                         false);
             }
         }
     }
 
     public void nLP() {
         double sRow[] = new double[finalDeath + 1];
         for (int i = 0; i < finalDeath; i++) {
             sRow[i] = lpAssets.getLpValue(i);
         }
         netWMap.put("Limited Partner Retained Interest in FLP", sRow);
     }
 
     public void nMortgage() {
         double sRow[] = new double[finalDeath + 1];
         boolean rFlag = false;
         for (int i = 0; i < finalDeath; i++) {
             sRow[i] = -mortValues.getLiability(i);
             if (sRow[i] != 0) {
                 rFlag = true;
             }
         }
         if (rFlag) {
             netWMap.put("Mortgages and other Liabilities", sRow);
         }
     }
 
     public void nPersonal() {
         // OK first get all of the assets
         EstatePlanAssets epa = new EstatePlanAssets(scenarioId);
         ArrayList<EPTAssetBean> bList = epa.getAssetTypes(9); // 9
 
         LoadAssetBean lab = new LoadAssetBean();
 
         Iterator<EPTAssetBean> i = bList.iterator();
         while (i.hasNext()) {
             boolean keepFlag = false;
             EPTAssetBean eab = (EPTAssetBean) i.next();
             eab.setDbObject();
             double rValue = eab.getRemainingValue();
             if (rValue > 0 && eab.getTclatId() <= 0) {
                 keepFlag = true;
             }
             if (rValue > 0) {
                 AssetSqlBean asb = lab.loadAssetBean(eab);
                 double iGrowth = asb.getAssetGrowth();
                 processNetWorthAsset(eab.getDescription(), rValue, iGrowth,
                         keepFlag);
             }
         }
     }
 
     public void nPersonalDebt() {
         EstatePlanAssets epa = new EstatePlanAssets(scenarioId);
         ArrayList<EPTAssetBean> bList = epa.getAssetTypes(4); // Debts
         LoadAssetBean lab = new LoadAssetBean();
         Iterator<EPTAssetBean> itr = bList.iterator();
         while (itr.hasNext()) {
             double sRow[] = new double[finalDeath + 1];
             EPTAssetBean eab = (EPTAssetBean) itr.next();
             eab.setDbObject();
             double rValue = eab.getRemainingValue();
             if (rValue > 0) {
                 DebtBean db = (DebtBean) lab.loadAssetBean(eab);
                 double value = db.getValue();
                 double interestRate = db.getInterestRate();
                 int years = (int) db.getLoanTerm();
                 double pmt = Function.PMT(value, interestRate, years);
                 for (int i = 0; i < finalDeath; i++) {
                     double interestPayment = Function.IPMT(interestRate, i,
                             years, value);
                     value = value - (pmt - interestPayment);
                     if (value < 0) {
                         value = 0;
                     }
                     sRow[i] = -value; // Because this is a debt
                 }
                 netWMap.put(db.getDescription(), sRow);
             }
         }
     }
 
     public void nQprt() {
         for (QprtTool qprt : qprtToolList) {
             double assetValue = 0;
             double assetGrowth = 0;
             double sRow[] = new double[finalDeath + 1];
             assetValue = qprt.getValue();
             assetGrowth = qprt.getGrowth();
 
             // If we have two trusts, than do each separate as
             // each may have a different term length and start date;
             // We add the value of the qprt until the term+start
             if (qprt.getNumberOfTrusts() > 2) {
                 double aValue = assetValue / 2.0;		//split the value
                 double start = qprt.getClientStartTerm();
                 if (start <= 1) {
                     start = 0;
                 }
                 for (int i = 0; i < qprt.getClientTerm() + start; i++) {
                     aValue = aValue * (1 + assetGrowth);
                     sRow[i] += aValue;
                 }
                 // Reset aValue for spouse
                 aValue = assetValue / 2.0;
                 start = qprt.getSpouseStartTerm();
                 if (start <= 1) {
                     start = 0;
                 }
                 for (int i = 0; i < qprt.getSpouseTerm() + start; i++) {
                     aValue = aValue * (1 + assetGrowth);
                     sRow[i] += aValue;
                 }
             } else {		// We only have one trust
                 double aValue = assetValue;
                 double start = qprt.getClientStartTerm();
                 if (start <= 1) {
                     start = 0;
                 }
                 for (int i = 0; i < qprt.getSpouseTerm() + start; i++) {
                     aValue = aValue * (1 + assetGrowth);
                     sRow[i] += aValue;
                 }
             }
             String desc = "QPRT " + qprt.getAssetName();
             netWMap.put(desc, sRow);
         }
     }
 
     public void nRealEstate() {
 
         // OK first get all of the assets
         EstatePlanAssets epa = new EstatePlanAssets(scenarioId);
         ArrayList<EPTAssetBean> bList = epa.getAssetTypes(8); // 8 = Real Estate
 
         LoadAssetBean lab = new LoadAssetBean();
 
         Iterator<EPTAssetBean> i = bList.iterator();
         while (i.hasNext()) {
             EPTAssetBean eab = (EPTAssetBean) i.next();
             eab.setDbObject();
             double rValue = eab.getRemainingValue();
             if (rValue > 0) {
                 AssetSqlBean asb = lab.loadAssetBean(eab);
                 double iGrowth = asb.getAssetGrowth();
                 processNetWorthAsset(eab.getDescription(), rValue, iGrowth,
                         false);
             }
         }
     }
 
     public void nRetire() {
         double sRow[] = new double[finalDeath + 1];
         EstatePlanAssets epa = new EstatePlanAssets(scenarioId);
         ArrayList<EPTAssetBean> bList = epa.getAssetTypes(6);
         RetirementCashFlow rcf = new RetirementCashFlow();
         rcf.setClientAge(clientAge);
         rcf.setSpouseAge(spouseAge);
         rcf.setSingle(isSingle);
 
         LoadAssetBean lab = new LoadAssetBean();
         Iterator<EPTAssetBean> itr = bList.iterator();
         while (itr.hasNext()) {
             EPTAssetBean eab = (EPTAssetBean) itr.next();
             eab.setDbObject();
             double rValue = eab.getRemainingValue();
             if (rValue > 0) {
                 AssetSqlBean asb = lab.loadAssetBean(eab);
                 double remainingValue = asb.getAssetValue();
                 double growthRate = asb.getAssetGrowth();
                 rcf.addPlan(remainingValue, growthRate);
             }
         }
 
         double[] rV = rcf.getTotalValue();
         boolean rFlag = false;
 
         for (int i = 0; i < finalDeath; i++) {
             double v = rpmValues.getValue(i) + rV[i];
             sRow[i] = v;
             if (v > 1000) {
                 rFlag = true;
             }
         }
         if (rFlag) {
             netWMap.put("Retirement Plans", sRow);
         }
     }
 
     public void nScin() {
         Iterator<SCINTool> itr = scinArray.iterator();
         while (itr.hasNext()) {
             SCINTool s = (SCINTool) itr.next();
             double sRow[] = s.getNote();
             netWMap.put("SCIN Note", sRow);
         }
     }
 
     public void nSecurities() {
         double sRow[] = new double[finalDeath + 1];
         for (int i = 0; i < finalDeath; i++) {
             sRow[i] = securities.getEndBalance(i);
         }
         netWMap.put("Sec Portfolio", sRow);
     }
 
     public void nSidit() {
         Iterator<SIditTool> itr = siditArray.iterator();
         while (itr.hasNext()) {
             SIditTool s = (SIditTool) itr.next();
             double n = s.getDmv();
             double sRow[] = new double[finalDeath + 1];
             for (int i = 0; i < s.getNoteTerm() - 1; i++) {
                 sRow[i] = n;
             }
             netWMap.put("IDIT Note", sRow);
         }
     }
 
     public void processNetWorthAsset(String description, double value,
             double growth, boolean keep) {
         double sRow[] = new double[finalDeath + 1];
 
         sRow[0] = value * (1 + growth);
         if (keep) {
             excludePersonal[0] += sRow[0];
         }
 
         for (int i = 1; i < finalDeath; i++) {
             sRow[i] = sRow[i - 1] * (1 + growth);
             if (keep) {
                 excludePersonal[i] += sRow[i];
             }
         }
         netWMap.put(description, sRow);
     }
 }
