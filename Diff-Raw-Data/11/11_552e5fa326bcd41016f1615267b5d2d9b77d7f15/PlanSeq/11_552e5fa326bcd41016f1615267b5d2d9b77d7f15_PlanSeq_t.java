 package apapl.plans;
 
 
 import apapl.*;
 import apapl.data.*;
 import apapl.program.*;
 import java.util.*;
 
 /**
  * A sequence of plans. A PlanSeq has a {@link apapl.program.PGrule}, the
  * activation rule that generated this plan sequence together with the substitutions 
  * that were applied on the head and guard in generating this PlanSeq. If this rule 
  * is null, it has been initially adopted by the module. The plans that occur inside 
  * the PlanSeq can be iterated over by an iterator.
  */
 public class PlanSeq implements ParentPlan, Iterable<Plan>, Substitutable
 {
 	private LinkedList<Plan> plans = new LinkedList<Plan>();
 	
 	private PGrule activationRule = null;
 	private SubstList<Term> activationGoal = null;
 	private SubstList<Term> activationSubstitution = null;
 	
 	private int id;
 	private static int id_counter = 0;
 	
 	private Date deadline;
 	private Date executionStart;
 	private long duration;
 	
 	protected boolean isAtomic = false;
 
 	private byte priority;
 	
 	private boolean scheduled = false;
 
 	private boolean prohibited = false;
 	
 	/**
 	 * Constructs a sequence of plans.
 	 */
 	public PlanSeq()
 	{
		id = id_counter;
 		id_counter++;
 	}
 	
 	/**
 	 * Constructs a sequence of plans.
 	 * 
 	 * @param plans the plans this plan sequence consists of
 	 */
 	public PlanSeq(LinkedList<Plan> plans)
 	{
		id = id_counter;
 		id_counter++;
 		this.plans = plans;
 	}
 	
 	/**
 	 * Constructs a sequence of plans.
 	 * 
 	 * @param activationRule the rule that activated this plan
 	 * @param activationGoal the substitution needed for activation
 	 */
 	public PlanSeq(PGrule activationRule, SubstList<Term> activationGoal)
 	{
 		this.activationRule = activationRule;
 		this.activationGoal = activationGoal;
 	}
 	
 	/**
 	 * Sets the PG-rule that activated this plan sequence.
 	 * @param activationRule the rule
 	 */
 	public void setActivationRule(PGrule activationRule)
 	{
 		this.activationRule = activationRule;
 	}
 	
 	/**
 	 * Sets the substitution that was used for the head of the activation rule.
 	 * 
 	 * @param activationGoal the substitution
 	 */
 	public void setActivationGoal(SubstList<Term> activationGoal)
 	{
 		this.activationGoal = activationGoal;
 	}
 	
 	/**
 	 * Sets the substitution that was used for the guard of the activiation rule.
 	 * 
 	 * @param activationSubstitution
 	 */
 	public void setActivationSubstitution(SubstList<Term> activationSubstitution)
 	{
 		this.activationSubstitution = activationSubstitution;
 	}
 	
 	/**
 	 * Returns the activation rule.
 	 * 
 	 * @return the activation rule
 	 */
 	public PGrule getActivationRule()
 	{
 		return activationRule;
 	}
 	
 	/**
 	 * Returns the substitution that was used for the guard of the activiation rule.
 	 * 
 	 * @return the substitution
 	 */
 	public SubstList<Term> getActivationSubstitution()
 	{
 		return activationSubstitution;
 	}
 	
 	/**
 	 * Returns the substitution that was used for the head of the activiation rule.
 	 * 
 	 * @return the substitution
 	 */
 	public SubstList<Term> getActivationGoal()
 	{
 		return activationGoal;
 	}
 	
 	/**
 	 * Construct for adding plans. Only called when this plan is build from the parser.
 	 */
 	public void addPlan(Plan p)
 	{
 		p.setParent(this);
 		plans.addLast(p);
 	}
 	
 	/**
 	 * Returns the identifier of this plan.
 	 * 
 	 * @return the identifier
 	 */
 	public int getID()
 	{
		return id;
 	}
 	
 	/**
 	 * Removes the first plan in this plan sequence.
 	 */
 	public void removeFirst()
 	{
 		plans.removeFirst();
 	}
 	
 	/**
 	 * Adds a plan sequence to this plan sequence. This method is called by the deliberation 
 	 * when a plan needs to be substituted.
 	 * 
 	 * @param the plan sequence to be added
 	 */
 	public void addFirst(PlanSeq ps)
 	{
 		for (Plan plan : ps.getPlans()) plan.setParent(this);
 		plans.addAll(0,ps.getPlans());
 	}
 	
 	/**
 	 * Adds a plan at the end of this plan sequence.
 	 * 
 	 * @param p the plan to be added.
 	 */
 	public void addLast(Plan p)
 	{
 		p.setParent(this);
 		plans.addLast(p);
 	}
 	
 	/**
 	 * Adds a plan to this plan. This method is called by the deliberation when 
 	 * a plan needs to be substituted.
 	 */
 	public void addLast(PlanSeq ps)
 	{
 		for (Plan plan : ps.getPlans()) plan.setParent(this);
 		plans.addAll(ps.getPlans());
 	}
 	
 	/**
 	 * Checks whether the plan sequence contains no plans.
 	 * 
 	 * @return true if empty, false otherwise
 	 */
 	public boolean isEmpty()
 	{
 		return plans.size()==0;
 	}
 	
 	
 	/**
 	 * Returns whether this plan can be shown on one line.
 	 * 
 	 * @return true if this plan can be shown on one line, false otherwise
 	 */
 	public boolean oneliner()
 	{
 		return
 			( (plans.size()==1)
 			&& !(plans.get(0) instanceof ChunkPlan)
 			&& !(plans.get(0) instanceof ConditionalPlan)
 			&& !(plans.get(0) instanceof WhilePlan)
 			); 
 	}
 
 
 	/**
 	 * Tests the goal that activated this plan to the goalbase.
 	 * 
 	 * @return true if the activation goal of this plan is still a goal.
 	 */
 	public boolean testActivationGoal(Goalbase gb, Beliefbase bb)
 	{
 	  // If plan has been triggered by an event (activationRule == null) 
 		// or has been triggered as a consequence of a reactive rule (True)
 		// then return true.
 		if (activationRule==null || activationRule.getHead() instanceof True)
 		{ return( true );
 		}
 		
 		Query q = activationRule.getHead().clone();
 		q.applySubstitution(activationGoal);
 		
 		SolutionIterator solutions = bb.doTest(q);
 		return !(solutions.hasNext());
 	}
 	
 	/**
 	 * Executes the first plan of this list.
 	 * 
 	 * @param module the owner module of this action.
 	 * @return true if the plan was succesfully executed, false otherwise
 	 */
 	public boolean execute(APLModule module) throws ActivationGoalAchievedException, ModuleDeactivatedException
 	{
 		Goalbase gb = module.getGoalbase();
 		Beliefbase bb = module.getBeliefbase();
 		if (!testActivationGoal(gb,bb)) 
 			throw new ActivationGoalAchievedException();
 	
 		if (plans.size()<=0)
 		{	return false;
 		}
 		else 
 		{	
 
 			
 			Plan p = plans.getFirst();
 			PlanResult r = p.execute(module);
 			
 			if (r.failed()) {
				module.notifyIEvent(getID());
 			}
 					
 			//The second part of this disjunction is to prevent the deliberation to block
 			//if there is only an external action executed but failed and there is no PR rule for that plan.
 			return (!r.failed() && !r.noEffect()) || (p instanceof ExternalAction); 
 		}
 	}
 	
 	/**
 	 *  Removes all chunks (atomic blocks) from this plan.
 	 */
 	public void unChunk()
 	{
 		LinkedList<Plan> unchunkplans = new LinkedList<Plan>();
 		
 		try	{
 			for (Plan p : plans)
 			if (p instanceof ChunkPlan)	{
 				LinkedList<Plan> cps = ((ChunkPlan)p).getPlans();
 				for (Plan cp : cps) cp.setParent(this);
 				unchunkplans.addAll(cps);
 			}
 			else unchunkplans.add(p);
 
 		}
 		catch (NoSuchElementException e) {}
 		plans = unchunkplans;
 	}
 	
 	/**
 	 * Returns a list of plans this plan sequence is built of.
 	 * 
 	 * @return the list of plans
 	 */
 	public LinkedList<Plan> getPlans()
 	{
 		return plans;
 	}
 	
 	public String toString()
 	{
 		return Base.concatWith(plans,"; ");
 	}
 	
 	public String toScopeString()
 	{
 		if (plans.size()==1) return toString();
 		else return "{"+toString()+"}";
 	}
 	
 	/**
 	 * Applies a substitution to this plan.
 	 * 
 	 * @param theta the substitution to apply
 	 */
 	public void applySubstitution(SubstList<Term> theta)
 	{
 		for (Plan p : plans) p.applySubstitution(theta);
 	}
 	
 	/**
 	 * Clones this plan sequence.
 	 * 
 	 * @return the clone
 	 */
 	public PlanSeq clone()
 	{
 		PlanSeq copy = new PlanSeq();
 		for (Plan p : plans) copy.addPlan(p.clone());
 		return copy;
 	}
 	
 	/**
 	 * Removes all plans that occur in this plan sequence.
 	 */
 	public void removeAll()
 	{
 		plans = new LinkedList<Plan>();
 	}
 	
 	/**
 	 * Pretty print, displays this plan sequence in a readable fashion.
 	 * 
 	 * @param t
 	 * @return
 	 */
 	public String pp(int t)
 	{
 		if (plans.size()==1&&false) return plans.get(0).pp(t);
 		
 		String s = "{"+"\t";
 		for (Plan p : plans)
 			s = s + p.pp(t+1)+";\n"+Base.tabs(t+1);
 		
 		if (s.length()>=(t+3)) s = s.substring(0,s.length()-(t+3));	
 		
 		return s+"\n"+Base.tabs(t)+"}"; //+extrainfo;
 	}
 	
 	public String toRTF()
 	{
 		if (plans.size()==1) return plans.get(0).toRTF(true);
 		
 		String s = "\\{ ";
 		for (Plan p : plans)
 			s = s + p.toRTF(true)+"\\cf1 ; \\cf0  ";
 		
 		if (s.length()>=12) s = s.substring(0,s.length()-12);	
 						
 		return s+" \\}";
 	}
 	
 	public String toRTF(boolean inplan)
 	{
 		return toRTF();
 	}
 	
 	public String toRTF(int t)
 	{
 		//if (plans.size()==1) return plans.get(0).toRTF(t);
 		
 		String s = "\\{\\tab ";
 		for (Plan p : plans)
 			s = s + p.toRTF(t+1)+"\\cf1 ; \\cf0 \\par\n"+Base.rtftabs(t+1);
 		int l = ("\\cf1 ; \\cf0 \\par\n"+Base.rtftabs(t+1)).length();
 		if (s.length()>=l) s = s.substring(0,s.length()-l);	
 		
 		String extrainfo = "";
 		/*String extrainfo = "\\par\n\\cf5 "
 			+Base.rtftabs(t+1)+"ID = "+id+"\\par\n"
 			+Base.rtftabs(t+1)+"rule = "+activationRule+"\\par\n"
 			+Base.rtftabs(t+1)+"goal = "+activationGoal+"\\cf0 ";*/
 		return s+ extrainfo + "\\par\n"+Base.rtftabs(t)+"\\}";
 	}
 
 	/**
 	 * Turn all variables of this plan sequence into fresh (unique) variables.
 	 * 
 	 * @param unfresh the list of variables that cannot be used anymore
 	 * @param own all the variables in the context of this plan sequence
 	 * @param changes list [[old,new],...] of substitutions
 	 */
 	public void freshVars(ArrayList<String> unfresh, ArrayList<String> own, ArrayList<ArrayList<String>> changes)
 	{
 		for (Plan p : plans) p.freshVars(unfresh,own,changes);
 	}
 	
 	/**
 	 * Returns all variables that occur in this plan sequence.
 	 * 
 	 * @return list of variables
 	 */
 	public ArrayList<String> getVariables()
 	{
 		ArrayList<String> vars = new ArrayList<String>();
 		for (Plan p : plans) vars.addAll(p.getVariables());
 		return vars;
 	}
 	
 	/**
 	 * Returns all plan variables that occur in this plan sequence. A plan sequence
 	 * might contain plan variables if it is used as pattern of the head a 
 	 * {@link apapl.program.PRrule}.
 	 *  
 	 * @return list of plan variables
 	 */
 	public ArrayList<String> getPlanVariables()
 	{
 		ArrayList<String> vars = new ArrayList<String>();
 		for (Plan p : plans) 
 		{ if (p instanceof ConditionalPlan) vars.addAll(((ConditionalPlan)p).getPlanVariables());
 			else if (p instanceof WhilePlan) vars.addAll(((WhilePlan)p).getPlanVariables());
 			else if (p instanceof ChunkPlan) vars.addAll(((ChunkPlan)p).getPlanVariables());
 			else if (p instanceof PlanVariable) vars.add(((PlanVariable)p).getName());
 		}
 		return vars;
 	}
 	
 	/**
 	 * Returns an iterator to iterate over the plans in this plan sequence.
 	 * 
 	 * @return an iterator
 	 */
 	public Iterator<Plan> iterator()
 	{
 		return plans.iterator();
 	}
 	
 	/**
 	 * Checks the plan sequence and generates warnings. Checks whether variables
 	 * that must become bounded can actually become bounded.
 	 * 
 	 * @param warnings a list that is filled with warnings by this method
 	 */
 	public void checkVars(LinkedList<String> warnings)
 	{
 		ArrayList<String> problemVars = new ArrayList<String>();
 		ArrayList<String> mustBeBounded = new ArrayList<String>();
 		ArrayList<String> canBeBounded = new ArrayList<String>();
 		for (Plan p : plans) {
 			mustBeBounded.addAll(p.mustBeBounded());
 			canBeBounded.addAll(p.canBeBounded());
 		}
 			
 		for (String c : mustBeBounded) if (!canBeBounded.contains(c)) problemVars.add(c);
 		
 		if (problemVars.size()==1) {
 			warnings.add("Variable \\b "+problemVars.get(0)+"\\b0  can not become bounded in plan " + toRTF());
 		}
 		else if (problemVars.size()>1) {
 			String warning = "Variables ";
 			int s = problemVars.size();
 			for (int i=0; i<problemVars.size()-2; i++) warning = warning + "\\b " + problemVars.get(i) + "\\b0, ";
 			warning = warning + "\\b " + problemVars.get(s-2) + "\\b0  and \\b " + problemVars.get(s-1) + "\\b0 ";
 			warnings.add(warning+" can not become bounded in plan" + toRTF());
 		}
 	}
 	
 	/**
 	 * Applies a substitution to this plan sequence.
 	 * 
 	 * @param theta the substitution to apply
 	 */
 	public void applyPlanSubstitution(SubstList<PlanSeq> theta)
 	{
 		LinkedList<Plan> newPlans = new LinkedList<Plan>();
 		for (Plan p : plans) {
 			if (p instanceof PlanVariable)	{
 				PlanSeq s = theta.get(((PlanVariable)p).getName());
 				if (s!=null) for (Plan p2 : s) {
 					p2.setParent(this);
 					newPlans.addLast(p2);
 				}
 				else newPlans.addLast(p);
 			}
 			else if (p instanceof ConditionalPlan) {
 				ConditionalPlan v = (ConditionalPlan)p;
 				PlanSeq thenPlan = v.getThenPlan();
 				PlanSeq elsePlan = v.getElsePlan();
 				thenPlan.applyPlanSubstitution(theta);
 				elsePlan.applyPlanSubstitution(theta);
 				Plan s = new ConditionalPlan(v.getCondition(),thenPlan,elsePlan);
 				newPlans.addLast(s);
 			}
 			else if (p instanceof WhilePlan) {
 				WhilePlan v = (WhilePlan)p;
 				PlanSeq plan = v.getPlan();
 				plan.applyPlanSubstitution(theta);
 				Plan s = new WhilePlan(v.getCondition(),plan);
 				newPlans.addLast(s);
 			}
 			else if (p instanceof ChunkPlan) {
 				((ChunkPlan)p).applyPlanSubstitution(theta);
 				newPlans.addLast(p);
 			}
 			else newPlans.addLast(p);
 		}
 		plans = newPlans;
 	}
 	
 	public void setDeadline(Date deadline2)
 	{
 		this.deadline = deadline2;
 	}
 	
 	public Date getDeadline()
 	{
 		return this.deadline;
 	}
 
 	public long getDuration() {
 
 		return duration;
 	}
 
 	public void setDuration(Long duration2) {
 
 		duration = duration2;
 	}
 
 	public Date getExecStart() {
 
 		return this.executionStart;
 	}
 
 	public void setExecStart(Date date) {
 		if (date == null)
 			this.executionStart = new Date();
 		else
 			this.executionStart = date;
 	}
 
 
 
 	public Byte getPriority() {
 
 		return this.priority;
 	}
 
 	public void setPriority(Byte priority) {
 		
 		this.priority = priority;
 	}
 
 	public boolean isScheduled() {
 
 		return this.scheduled;
 	}
 
 	public void setScheduled(boolean b) {
 		this.scheduled = b;
 	}
 
 	/**
 	 * @return the isAtomic
 	 */
 	public boolean isAtomic() {
 		return isAtomic;
 	}
 
 	/**
 	 * @param isAtomic the isAtomic to set
 	 */
 	public void setAtomic(boolean b) {
 		this.isAtomic = b;
 	}
 	public void setAtomic() {
 		this.isAtomic = true;
 	}
 
 	public void setProhibited() {
 		this.prohibited  = true;
 		
 	}
 	
 	public boolean getProhibited() {
 		return this.prohibited;
 		
 	}
 }
