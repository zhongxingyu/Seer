 package codemate.templates;
 
 /**
  * FortranListTemplate
  *
  * This template class provides double linked list type for using in Fortran.
  *
  * For element type:
  *
  *     type, extends(list_elem_t<foo_t>) :: foo_t
  *         ...
  *     end type foo_t
  *
  * For container type:
  *
  *     type(list_t<foo_t>) foos
  *
  * @author      Li Dong <dongli@lasg.iap.ac.cn>
  */
 
 import codemate.Fortran.*;
 import java.util.*;
 
 public class FortranListTemplate implements TemplateBundle {
     public boolean containsTemplate(String templateName) {
         if (templateName.equals("list_elem_t") ||
             templateName.equals("list_t"))
             return true;
         else
             return false;
     }
 
     public Map<Template, String> instantiate(String templateName,
             List<String> args, String block) {
         if (templateName.equals("list_elem_t"))
             return instantiateListElem(args, block);
         else if (templateName.equals("list_t"))
             return instantiateList(args, block);
         else
             return null;
     }
 
     public Map<Template, String> instantiateListElem(List<String> args, String block) {
         String elemTypeName = args.get(0);
         String extendedDataDeclarationStatements = args.get(1);
         String extendedTypeBoundProcedures = args.get(2);
         String listTypeName = "list_"+elemTypeName;
 
         String instance;
         Map<Template, String> instances = new HashMap<Template, String>();
 
        instance = "public "+listTypeName+"\n";
        instances.put(new Template(FortranParser.RULE_accessibilityStatements,
        		Template.Action.PREPEND), instance);
        
         instance =
         	// list element type declaration
             "type "+elemTypeName+"\n"+
             extendedDataDeclarationStatements+"\n"+
             "    integer, private :: elem_id\n"+
             "    type("+elemTypeName+"), private, pointer :: prev, next\n"+
             "contains\n"+
             extendedTypeBoundProcedures+"\n"+
             "    procedure :: get_elem_id => "+elemTypeName+"_get_elem_id\n"+
             "    procedure :: get_prev => "+elemTypeName+"_get_prev\n"+
             "    procedure :: get_next => "+elemTypeName+"_get_next\n"+
             "end type "+elemTypeName+"\n"+
             "\n"+
         	// list type declaration
         	"type "+listTypeName+"\n"+
         	"    integer, private :: num_elem = 0\n"+
         	"    type("+elemTypeName+"), private, pointer :: head, tail\n"+
         	"contains\n"+
         	"    procedure :: get_num_elem => "+listTypeName+"_get_num_elem\n"+
         	"    procedure :: get_start_elem => "+listTypeName+"_get_start_elem\n"+
         	"    procedure :: get_end_elem => "+listTypeName+"_get_end_elem\n"+
         	"    procedure :: get_elem_at => "+listTypeName+"_get_elem_at\n"+
         	"    procedure :: create => "+listTypeName+"_create\n"+
         	"    procedure :: insert => "+listTypeName+"_insert\n"+
         	"    procedure :: remove => "+listTypeName+"_remove\n"+
         	"    procedure :: destroy => "+listTypeName+"_destroy\n"+
         	"end type "+listTypeName+"\n";
         instances.put(new Template(FortranParser.RULE_declarationStatements,
         		Template.Action.REPLACE), instance);
 
         instance =
             "function "+elemTypeName+"_get_elem_id(this) result(res)\n"+
             "\n"+
             "    class("+elemTypeName+"), intent(in) :: this\n"+
             "    integer res\n"+
             "\n"+
             "    res = this%elem_id\n"+
             "\n"+
             "end function "+elemTypeName+"_get_elem_id\n"+
             "\n"+
             "subroutine "+elemTypeName+"_get_prev(this, res)\n"+
             "\n"+
             "    class("+elemTypeName+"), intent(in) :: this\n"+
             "    type("+elemTypeName+"), intent(out), pointer :: res\n"+
             "\n"+
             "    res => this%prev\n"+
             "\n"+
             "end subroutine "+elemTypeName+"_get_prev\n"+
             "\n"+
             "subroutine "+elemTypeName+"_get_next(this, res)\n"+
             "\n"+
             "    class("+elemTypeName+"), intent(in) :: this\n"+
             "    type("+elemTypeName+"), intent(out), pointer :: res\n"+
             "\n"+
             "    res => this%next\n"+
             "\n"+
             "end subroutine "+elemTypeName+"_get_next\n"+
             "\n"+
             "function "+listTypeName+"_get_num_elem(this) result(res)\n"+
         	"\n"+
         	"    class("+listTypeName+"), intent(in) :: this\n"+
         	"    integer res\n"+
         	"\n"+
         	"    res = this%num_elem\n"+
         	"\n"+
         	"end function "+listTypeName+"_get_num_elem\n"+
         	"\n"+
         	"subroutine "+listTypeName+"_get_start_elem(this, res)\n"+
         	"\n"+
         	"    class("+listTypeName+"), intent(in) :: this\n"+
         	"    type("+elemTypeName+"), intent(out), pointer :: res\n"+
         	"\n"+
         	"    res => this%head\n"+
         	"\n"+
         	"end subroutine "+listTypeName+"_get_start_elem\n"+
         	"\n"+
         	"subroutine "+listTypeName+"_get_end_elem(this, res)\n"+
         	"\n"+
         	"    class("+listTypeName+"), intent(in) :: this\n"+
         	"    type("+elemTypeName+"), intent(out), pointer :: res\n"+
         	"\n"+
         	"    res => this%tail\n"+
         	"\n"+
         	"end subroutine "+listTypeName+"_get_end_elem\n" +
         	"\n"+
         	"subroutine "+listTypeName+"_get_elem_at(this, idx, res)\n"+
         	"\n"+
         	"    class("+listTypeName+"), intent(in) :: this\n"+
         	"    integer, intent(in) :: idx\n"+
         	"    type("+elemTypeName+"), intent(out), pointer :: res\n"+
         	"\n"+
         	"    integer i\n"+
         	"\n"+
         	"    call this%get_start_elem(res)\n"+
         	"    do i = 1, this%get_num_elem()\n"+
         	"        if (i == idx) then\n"+
         	"            return\n"+
         	"        end if\n"+
         	"        call res%get_next(res)\n"+
         	"    end do\n"+
         	"    res => null()\n"+
         	"\n"+
         	"end subroutine "+listTypeName+"_get_elem_at\n"+
         	"\n"+
         	"subroutine "+listTypeName+"_create(this, num_elem)\n"+
         	"\n"+
         	"    class("+listTypeName+"), intent(inout) :: this\n"+
         	"    integer, intent(in) :: num_elem\n"+
         	"\n"+
         	"    type("+elemTypeName+"), pointer :: elem\n"+
         	"    integer i\n"+
         	"\n"+
         	"    do i = 1, num_elem\n"+
         	"        call this%insert(elem)\n"+
         	"    end do\n"+
         	"\n"+
         	"end subroutine "+listTypeName+"_create\n"+
         	"\n"+
         	"subroutine "+listTypeName+"_insert(this, elem, elem_anchor)\n"+
         	"\n"+
         	"    class("+listTypeName+"), intent(inout) :: this\n"+
         	"    type("+elemTypeName+"), intent(inout), pointer :: elem\n"+
         	"    type("+elemTypeName+"), intent(inout), pointer, optional :: elem_anchor\n"+
         	"\n"+
         	"    if (.not. present(elem_anchor)) then\n"+
         	"        if (this%num_elem == 0) then\n"+
         	"            allocate(this%head)\n"+
         	"            nullify(this%head%prev)\n"+
         	"            nullify(this%head%next)\n"+
         	"            this%tail => this%head\n"+
         	"        else\n"+
         	"            allocate(this%tail%next)\n"+
         	"            this%tail%next%prev => this%tail\n"+
         	"            this%tail => this%tail%next\n"+
         	"        end if\n"+
         	"        elem => this%tail\n"+
         	"    else\n"+
         	"        allocate(elem)\n"+
         	"        elem%prev => elem_anchor\n"+
         	"        elem%next => elem_anchor%next\n"+
         	"        elem_anchor%next%prev => elem\n"+
         	"        elem_anchor%next => elem\n"+
         	"    end if\n"+
         	"    this%num_elem = this%num_elem+1\n"+
         	"    elem%elem_id = this%num_elem\n"+
         	"\n"+
         	"end subroutine "+listTypeName+"_insert\n"+
         	"\n"+
         	"subroutine "+listTypeName+"_remove(this, elem)\n"+
         	"\n"+
         	"    class("+listTypeName+"), intent(inout) :: this\n"+
         	"    type("+elemTypeName+"), intent(inout), pointer :: elem\n"+
         	"\n"+
         	"    if (associated(elem%prev)) then\n"+
         	"        elem%prev%next => elem%next\n"+
         	"    end if\n"+
         	"    if (associated(elem%next)) then\n"+
         	"        elem%next%prev => elem%prev\n"+
         	"    end if\n"+
         	"    this%num_elem = this%num_elem-1\n"+
         	"    deallocate(elem)\n"+
         	"\n"+
         	"end subroutine "+listTypeName+"_remove\n"+
         	"\n"+
         	"subroutine "+listTypeName+"_destroy(this)\n"+
         	"\n"+
         	"    class("+listTypeName+"), intent(inout) :: this\n"+
         	"\n"+
         	"    type("+elemTypeName+"), pointer :: elem\n"+
         	"    integer i\n"+
         	"\n"+
         	"    elem => this%head\n"+
         	"    do while (associated(elem))\n"+
         	"        if (associated(elem%next)) then\n"+
         	"            elem => elem%next\n"+
         	"            deallocate(elem%prev)\n"+
         	"        else\n"+
         	"            deallocate(elem)\n"+
         	"        end if\n"+
         	"    end do\n"+
         	"    this%num_elem = 0\n"+
         	"\n"+
         	"end subroutine "+listTypeName+"_destroy\n";
         instances.put(new Template(FortranParser.RULE_containedProcedures,
         		Template.Action.APPEND), instance);
 
         return instances;
     }
 
     public Map<Template, String> instantiateList(List<String> args, String block) {
         String elemTypeName = args.get(0);
 
         Map<Template, String> instances = new HashMap<Template, String>();
 
         instances.put(new Template(FortranParser.RULE_derivedTypeName,
         		Template.Action.REPLACE), "list_"+elemTypeName);
         
         return instances;
      }
 }
