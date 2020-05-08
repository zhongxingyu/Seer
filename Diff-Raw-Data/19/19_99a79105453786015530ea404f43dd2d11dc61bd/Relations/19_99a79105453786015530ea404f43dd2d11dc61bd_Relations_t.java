 package org.genericsystem.tracker.structure;
 
 import org.genericsystem.annotation.Components;
 import org.genericsystem.annotation.SystemGeneric;
 import org.genericsystem.annotation.constraints.SingularConstraint;
 import org.genericsystem.core.GenericImpl;
import org.genericsystem.tracker.structure.Types.Description;
 import org.genericsystem.tracker.structure.Types.Issues;
 import org.genericsystem.tracker.structure.Types.Priorities;
 import org.genericsystem.tracker.structure.Types.Statutes;
 import org.genericsystem.tracker.structure.Types.Users;
 
 public class Relations {
 
 	/**
 	 * 
 	 * Issues Relations
 	 * 
 	 */
 
 	@SystemGeneric
 	@Components({ Issues.class, Priorities.class })
 	@SingularConstraint
 	public static class IssuePrioriyRelation extends GenericImpl {
 
 	}
 
 	@SystemGeneric
 	@Components({ Issues.class, Statutes.class })
 	@SingularConstraint
 	public static class IssueStatusesRelation extends GenericImpl {
 
 	}
 
 	@SystemGeneric
 	@Components({ Issues.class, Users.class })
 	@SingularConstraint
 	public static class IssueAssigneeRelation extends GenericImpl {
 
 	}
 
 	@SystemGeneric
 	@Components({ Issues.class, Users.class })
 	@SingularConstraint
 	public static class IssueReporterRelation extends GenericImpl {
 
 	}

	@SystemGeneric
	@Components({ Issues.class, Description.class })
	@SingularConstraint
	public static class IssueDescriptionRelation extends GenericImpl {

	}
 }
