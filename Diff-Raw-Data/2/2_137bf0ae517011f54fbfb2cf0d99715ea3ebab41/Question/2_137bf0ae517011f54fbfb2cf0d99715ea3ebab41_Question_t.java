 package org.openxdata.designer.util;
 
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.Vector;
 
 import org.apache.pivot.collections.ArrayList;
 import org.apache.pivot.collections.List;
 import org.apache.pivot.collections.ListListener;
 import org.apache.pivot.collections.Sequence;
 import org.apache.pivot.util.ListenerList;
 import org.fcitmuk.epihandy.OptionDef;
 import org.fcitmuk.epihandy.QuestionDef;
 import org.fcitmuk.epihandy.RepeatQtnsDef;
 import org.openxdata.designer.idgen.DefaultIdGenerator;
 import org.openxdata.designer.idgen.ScarceIdGenerator;
 
 public class Question extends org.fcitmuk.epihandy.QuestionDef implements
 		List<PageElement>, PageElement {
 
 	private static Vector<PageElement> DYN_PROXY_LIST;
 	private ScarceIdGenerator questionIdGen;
 	private ScarceIdGenerator optionIdGen;
 
 	static {
 		DYN_PROXY_LIST = new Vector<PageElement>();
 		DYN_PROXY_LIST.add(new DynamicOptionProxy());
 	}
 
 	// We store here to be able to preserve them... QuestionDef 'optimizes'
 	RepeatQtnsDef repeatQuestions = new RepeatQtnsDef();
 	Vector<Option> options = new Vector<Option>();
 
 	{
 		// Ensure the repeat's nested vector isn't null
 		repeatQuestions.setQuestions(new Vector<Question>());
		optionIdGen = new DefaultIdGenerator(0, Short.MAX_VALUE);
 	}
 
 	public Question() {
 		setText("Unnamed Question");
 	}
 
 	void setQuestionIdGen(ScarceIdGenerator questionIdGen) {
 		this.questionIdGen = questionIdGen;
 	}
 
 	public void newQuestion() {
 		Question newQuestion = new Question();
 		newQuestion.setQuestionIdGen(questionIdGen);
 		short questionId = (short) questionIdGen.nextId();
 		newQuestion.setId(questionId);
 		add(newQuestion);
 	}
 
 	public void newOption() {
 		Option newOption = new Option();
 		short optionId = (short) optionIdGen.nextId();
 		newOption.setId(optionId);
 		add(newOption);
 	}
 
 	public Question(ScarceIdGenerator questionIdGen, QuestionDef questionDef) {
 		super(questionDef);
 
 		this.questionIdGen = questionIdGen;
 
 		if (isQuestionList()) {
 			RepeatQtnsDef repeatDef = super.getRepeatQtnsDef();
 			if (repeatDef != null) {
 				@SuppressWarnings("unchecked")
 				Vector<Question> nestedQuestions = (Vector<Question>) repeatDef
 						.getQuestions();
 				for (int i = 0; i < nestedQuestions.size(); i++) {
 					QuestionDef nestedQuestionDef = nestedQuestions.get(i);
 					Question question = new Question(this.questionIdGen,
 							nestedQuestionDef);
 					short questionId = (short) questionIdGen.nextId();
 					question.setId(questionId);
 					nestedQuestions.set(i, question);
 				}
 				this.repeatQuestions.setQuestions(nestedQuestions);
 			}
 		} else if (isStaticOptionList()) {
 			@SuppressWarnings("unchecked")
 			Vector<Option> options = (Vector<Option>) super.getOptions();
 			for (int i = 0; i < options.size(); i++) {
 				OptionDef optionDef = options.get(i);
 				Option option = new Option(optionDef);
 				short optionId = (short) optionIdGen.nextId();
 				option.setId(optionId);
 				options.set(i, option);
 			}
 			this.options = options;
 		}
 	}
 
 	@Override
 	public RepeatQtnsDef getRepeatQtnsDef() {
 		return repeatQuestions;
 	}
 
 	@Override
 	public Vector<? extends OptionDef> getOptions() {
 		return options;
 	}
 
 	private void setOptionsByType() {
 		if (isQuestionList())
 			setRepeatQtnsDef(this.repeatQuestions);
 		else if (isStaticOptionList())
 			setOptions(this.options);
 	}
 
 	@Override
 	public void setType(byte type) {
 
 		byte origType = getType();
 		Vector<PageElement> origList = getElements();
 		boolean containsList = isQuestionList() || isStaticOptionList()
 				|| isDynamicOptionList();
 		super.setType(type);
 
 		setOptionsByType();
 
 		boolean containsListAfter = isQuestionList() || isStaticOptionList()
 				|| isDynamicOptionList();
 
 		// Need check for null because we're called in super constructor
 		if (listenerList != null) {
 			if (containsList && !containsListAfter)
 				listenerList.listCleared(this);
 			else if (!containsList && containsListAfter)
 				for (int i = 0; i < getLength(); i++)
 					listenerList.itemInserted(this, i);
 			else if (containsList && containsListAfter && origType != getType()) {
 				if (origList.size() >= getLength()) {
 					for (int i = 0; i < getLength(); i++)
 						listenerList.itemUpdated(this, i, origList.get(i));
 					// Original list is larger or equal in size of current list
 					Sequence<PageElement> removedItems = new ArrayList<PageElement>();
 					for (int i = getLength(); i < origList.size(); i++)
 						removedItems.add(origList.get(i));
 					if (removedItems.getLength() > 0)
 						listenerList.itemsRemoved(this, getLength(),
 								removedItems);
 				} else {
 					// Original list is smaller than current
 					for (int i = 0; i < origList.size(); i++)
 						listenerList.itemUpdated(this, i, origList.get(i));
 					for (int i = origList.size(); i < getLength(); i++)
 						listenerList.itemInserted(this, i);
 				}
 
 			}
 		}
 	}
 
 	public boolean isQuestionList() {
 		byte questionType = getType();
 		return questionType == QTN_TYPE_REPEAT;
 	}
 
 	public boolean isStaticOptionList() {
 		byte questionType = getType();
 		return questionType == QTN_TYPE_LIST_EXCLUSIVE
 				|| questionType == QTN_TYPE_LIST_MULTIPLE;
 	}
 
 	public boolean isDynamicOptionList() {
 		byte questionType = getType();
 		return questionType == QTN_TYPE_LIST_EXCLUSIVE_DYNAMIC;
 	}
 
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	Vector<PageElement> getElements() {
 		Vector<PageElement> elements = new Vector<PageElement>();
 		if (isQuestionList()) {
 			elements = getRepeatQtnsDef().getQuestions();
 		} else if (isStaticOptionList()) {
 			elements = (Vector) getOptions();
 		} else if (isDynamicOptionList()) {
 			elements = DYN_PROXY_LIST;
 		}
 		return elements;
 	}
 
 	public boolean acceptsPageElement(PageElement element) {
 		if (isQuestionList() && element instanceof Question)
 			return true;
 		if (isStaticOptionList() && element instanceof Option)
 			return true;
 		return false;
 	}
 
 	public int remove(PageElement item) {
 
 		if (!acceptsPageElement(item))
 			return -1;
 
 		Vector<PageElement> elements = getElements();
 
 		if (elements == null)
 			return -1;
 
 		for (int i = 0; i < elements.size(); i++) {
 			if (item.equals(elements.get(i))) {
 				PageElement removedElement = elements.remove(i);
 				freePageElement(removedElement);
 				listenerList.itemsRemoved(this, i, new ArrayList<PageElement>(
 						removedElement));
 				return i;
 			}
 		}
 
 		return -1;
 	}
 
 	public PageElement get(int index) {
 		Vector<PageElement> pageElements = (Vector<PageElement>) getElements();
 		return (PageElement) pageElements.get(index);
 	}
 
 	public int indexOf(PageElement item) {
 		Vector<PageElement> pages = (Vector<PageElement>) getElements();
 		return pages.indexOf(item);
 	}
 
 	public boolean isEmpty() {
 		return getElements().isEmpty();
 	}
 
 	class PageElementComparator implements Comparator<PageElement> {
 
 		public int compare(PageElement o1, PageElement o2) {
 			if (o1 == o2 && o1 == null)
 				return 0;
 
 			if (o1 == null)
 				return -1;
 
 			if (o2 == null)
 				return 1;
 
 			return o1.toString().compareTo(o2.toString());
 		}
 
 	}
 
 	private Comparator<PageElement> comparator = new PageElementComparator();
 
 	public Comparator<PageElement> getComparator() {
 		return comparator;
 	}
 
 	public Iterator<PageElement> iterator() {
 		Vector<PageElement> elements = (Vector<PageElement>) getElements();
 		return elements.iterator();
 	}
 
 	public int add(PageElement item) {
 		Vector<PageElement> elements = (Vector<PageElement>) getElements();
 		int index = -1;
 		synchronized (elements) {
 			index = elements.size();
 			elements.add(item);
 			reservePageElement(item);
 			listenerList.itemInserted(this, index);
 			return index;
 		}
 	}
 
 	public void insert(PageElement item, int index) {
 		Vector<PageElement> elements = (Vector<PageElement>) getElements();
 		elements.insertElementAt(item, index);
 		reservePageElement(item);
 		listenerList.itemInserted(this, index);
 	}
 
 	public PageElement update(int index, PageElement item) {
 		Vector<PageElement> elements = (Vector<PageElement>) getElements();
 		PageElement exiled = null;
 		synchronized (elements) {
 			exiled = elements.get(index);
 			freePageElement(exiled);
 			elements.setElementAt(item, index);
 			reservePageElement(item);
 		}
 		listenerList.itemUpdated(this, index, exiled);
 		return exiled;
 	}
 
 	public Sequence<PageElement> remove(int index, int count) {
 		Vector<PageElement> elements = (Vector<PageElement>) getElements();
 		Sequence<PageElement> removedPageElements = new ArrayList<PageElement>();
 		for (int i = 0; i < count && index + i < elements.size(); i++) {
 			PageElement removedPageElement = elements.remove(index);
 			freePageElement(removedPageElement);
 			removedPageElements.add(removedPageElement);
 		}
 		listenerList.itemsRemoved(this, index, removedPageElements);
 		return removedPageElements;
 	}
 
 	private void reservePageElement(PageElement pageItem) {
 
 		if (pageItem instanceof Question) {
 			Question q = (Question) pageItem;
 			questionIdGen.reserveId(q.getId());
 			return;
 		}
 
 		if (pageItem instanceof Option) {
 			Option o = (Option) pageItem;
 			optionIdGen.reserveId(o.getId());
 			return;
 		}
 	}
 
 	private void freePageElement(PageElement pageItem) {
 
 		if (pageItem instanceof Question) {
 			Question q = (Question) pageItem;
 			questionIdGen.makeIdAvailable(q.getId());
 			return;
 		}
 
 		if (pageItem instanceof Option) {
 			Option o = (Option) pageItem;
 			optionIdGen.makeIdAvailable(o.getId());
 			return;
 		}
 	}
 
 	public void clear() {
 		Vector<PageElement> elements = (Vector<PageElement>) getElements();
 		for (PageElement pageItem : this)
 			freePageElement(pageItem);
 		elements.clear();
 	}
 
 	public int getLength() {
 		Vector<PageElement> elements = (Vector<PageElement>) getElements();
 		return elements.size();
 	}
 
 	public void setComparator(Comparator<PageElement> comparator) {
 		this.comparator = comparator;
 	}
 
 	private ListListenerList<PageElement> listenerList = new ListListenerList<PageElement>();
 
 	public ListenerList<ListListener<PageElement>> getListListeners() {
 		return listenerList;
 	}
 }
