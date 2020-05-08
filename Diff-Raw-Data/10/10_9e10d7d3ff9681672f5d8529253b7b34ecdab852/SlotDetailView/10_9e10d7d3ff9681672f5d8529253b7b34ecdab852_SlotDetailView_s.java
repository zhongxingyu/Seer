 package com.zybnet.abc.view;
 
 import java.sql.Time;
 
 import android.annotation.SuppressLint;
 import android.text.InputType;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 import com.zybnet.abc.R;
 import com.zybnet.abc.activity.AbbecedarioActivity;
 import com.zybnet.abc.controller.GradeController;
 import com.zybnet.abc.controller.HomeworkController;
 import com.zybnet.abc.controller.SubjectController;
 import com.zybnet.abc.controller.TeacherController;
 import com.zybnet.abc.model.Grade;
 import com.zybnet.abc.model.Homework;
 import com.zybnet.abc.model.MessageBus;
 import com.zybnet.abc.model.MessageBus.Action;
 import com.zybnet.abc.model.Slot;
 import com.zybnet.abc.model.Subject;
 import com.zybnet.abc.model.Subscriber;
 import com.zybnet.abc.model.Teacher;
 import com.zybnet.abc.utils.U;
 import com.zybnet.abc.view.EditView.Delegate;
 import com.zybnet.abc.view.NavigateBackView.Item;
 
 @SuppressLint("ViewConstructor")
 public class SlotDetailView extends LinearLayout {
 
 	public SlotDetailView(AbbecedarioActivity abc, HistoryViewFlipper flipper, SlotView slot) {
 		super(abc);
 		this.abc = abc;
 		this.flipper = flipper;
 		
 		setOrientation(VERTICAL);
 		U.setPaddingLeft(this, 10);
 		U.setPaddingRight(this, 10);
 		
 		abc.getLayoutInflater().inflate(R.layout.slot_detail, this);
 		fillView(slot);
 	}
 	
 	public void fillView(SlotView slotView) {
 		Slot slot = abc.db().getSlot(
 				slotView.getColumn() + 1,
 				slotView.getRow() + 1
 		);
 		fillView(slot);
 	}
 	
 	private OnClickListener itemListener = new OnClickListener() {
 		@Override
 		public void onClick(View v) {
 			Delegate helper;
 			int layout;
 			
 			switch(v.getId()) {
 			case R.id.title:
 				helper = shortNameDelegate.setup(R.string.edit_displayed, slot.display_text);
 				layout = R.layout.edit_item;
 				break;
 			case R.id.time:
 				helper = timeDelegate;
 				layout = R.layout.edit_time;
 				break;
 			case R.id.place:
 				helper = placeDelegate.setup(R.string.edit_place, slot.place);
 				layout = R.layout.edit_item;
 				break;
 			default:
 				throw new RuntimeException("Register listener for unknown ID");
 			}
 			
 			Item item = new Item(getContext());
 			item.opener = SlotDetailView.this;
 			item.view = new EditView(abc, layout, helper);
 			flipper.showView(item);
 		}
 	};
 	
 	private SingleStringDelegate shortNameDelegate = new SingleStringDelegate() {
 		
 		@Override
 		public void afterInflate(EditView view) {
 			super.afterInflate(view);
 			EditText et = (EditText) view.findViewById(R.id.content);
 			et.setInputType(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
 		}
 		
 		@Override
 		public void save(EditView view) {
 			Slot dst = new Slot(slot);
 			dst.display_text = ((TextView) view.findViewById(R.id.content)).getText().toString();
 			dst.save(abc.db());
 			flipper.back();
 		}
 	};
 	
 	private SingleStringDelegate placeDelegate = new SingleStringDelegate(){
 		
 		@Override
 		public void save(EditView view) {
 			Slot dst = new Slot(slot);
 			dst.place = ((TextView) view.findViewById(R.id.content)).getText().toString();
 			dst.save(abc.db());
 			flipper.back();
 		}
 		
 	};
 	
 	private Delegate timeDelegate = new Delegate() {
 		TimeEditText start, end;
 		
 		@Override
 		public void afterInflate(EditView view) {
 			start = (TimeEditText) view.findViewById(R.id.start);
 			end = (TimeEditText) view.findViewById(R.id.end);
 			
 			start.setTime(slot.start != null ? slot.start : Time.valueOf("08:00:00"));
 			end.setTime(slot.end != null ? slot.end : Time.valueOf("09:00:00"));
 			
 			start.setup(flipper);
 			end.setup(flipper);
 		}
 		
 		@Override
 		public void save(EditView view) {
 			Slot dst = new Slot(slot);
 			dst.start = start.getTime();
 			dst.end = end.getTime();
 			
 			dst.save(abc.db());
 			flipper.back();
 		}
 	};
 	
 	@Override
 	public void onAttachedToWindow() {
 		MessageBus.subscribe(Slot.class, slotSubscriber);
 		MessageBus.subscribe(Subject.class, subjectSubscriber);
 		MessageBus.subscribe(Teacher.class, teacherSubscriber);
 	}
 	
 	@Override
 	public void onDetachedFromWindow() {
 		MessageBus.unsuscribe(Slot.class, slotSubscriber);
 		MessageBus.unsuscribe(Subject.class, subjectSubscriber);
 		MessageBus.unsuscribe(Teacher.class, teacherSubscriber);
 	}
 	
 	private Subscriber<Slot> slotSubscriber = new Subscriber<Slot>() {
 		
 		@Override
 		public void onMessage(final Slot other, MessageBus.Action action) {
 			post(new Runnable() {
 				public void run() {
 					Slot self = getSlot();
 					
 					if (other.ord == self.ord && other.day == self.day) {
 						fillView(other);
 					}
 				}
 			});
 		}
 	};
 	
 	private Subscriber<Subject> subjectSubscriber = new Subscriber<Subject>() {
 
 		@Override
 		public void onMessage(Subject message, Action action) {
 			if (!message._id.equals(slot.subject_id))
 				return;
 			
			slot.subject_id = message._id;
 			fillView(slot);
 		}
 		
 	};
 	
 	private Subscriber<Teacher> teacherSubscriber = new Subscriber<Teacher>() {
 
 		@Override
 		public void onMessage(Teacher teacher, Action action) {
 			if (!teacher._id.equals(slot.teacher_id))
 				return;
 			
 			slot.teacher_id = teacher._id;
 			fillView(slot);
 		}
 		
 	};
 	
 	private class SingleStringDelegate extends EditView.Delegate {
 		
 		public int titleId;
 		public String value;
 		
 		@Override
 		public void afterInflate(EditView view) {
 			String title = getResources().getString(titleId);
 			((TextView) view.findViewById(R.id.title)).setText(title);
 			((TextView) view.findViewById(R.id.content)).setText(value);
 		}
 		
 		public SingleStringDelegate setup(int titleId, String value) {
 			this.titleId = titleId;
 			this.value = value;
 			return this;
 		}
 	}
 	
 	private Slot slot;
 	
 	public Slot getSlot() {
 		return slot;
 	}
 	
 	public void fillView(Slot slot) {
 		
 		this.slot = slot;
 		
 		View view = setText(R.id.title,
 				U.uppercaseFirstChar(String.format("%tA, slot %d",
 				U.getLocalizedDayOfTheWeek(slot.day), slot.ord)));
 		view.setOnClickListener(itemListener);
 		view.setTag(slot);
 		
 		Teacher teacher = abc.db().fill(Teacher.class, slot.teacher_id);
 		view = setText(R.id.teacher, teacher.name);
 		view.setOnClickListener(indexListener);
 		
 		view = setText(R.id.time,
 				(slot.start == null || slot.end == null) ?
 						null: String.format("%tR - %tR", slot.start, slot.end));
 		view.setOnClickListener(itemListener);
 		view.setTag(slot);
 		
 		view = setText(R.id.place, slot.place);
 		view.setTag(slot);
 		view.setOnClickListener(itemListener);
 		
 		Subject subject = abc.db().fill(Subject.class, slot.subject_id);
 		view = setText(R.id.subject, subject.name);
 		view.setOnClickListener(indexListener);
 		
 		View homework = findViewById(R.id.homework);
 		View grades = findViewById(R.id.grades);
 		
 		if (slot.subject_id == null) {
 			homework.setVisibility(View.INVISIBLE);
 			grades.setVisibility(View.INVISIBLE);
 		} else {
 			homework.setVisibility(View.VISIBLE);
 			homework.setOnClickListener(indexListener);
 			
 			grades.setVisibility(View.VISIBLE);
 			grades.setOnClickListener(indexListener);
 		}
 		
 	}
 	
 	private IndexView.OnItemPickedListener<Subject> pickedListener = new IndexView.OnItemPickedListener<Subject>() {
 
 		@Override
 		public void onItemPicked(Subject subject) {
 			Slot dst = new Slot(slot);
 			dst.subject_id = subject._id;
 			dst.display_text = subject.name_short;
 			dst.subject_id = subject._id;
 			dst.place = subject.default_place;
 			dst.teacher_id = subject.default_teacher_id;
 			dst.save(abc.db());
 			abc.getBackButton().back();
 		}
 	};
 	
 	private OnClickListener indexListener = new OnClickListener() {
 		
 		public void onClick(final View view) {
 			int viewId = view.getId();
 			
 			IndexView<?> index;
 			
 			String title;
 			
 			// TODO don't use hardcoded strings
 			if (viewId == R.id.subject) {
 				title = "Subjects";
 				SubjectController c = new SubjectController(abc, abc.db());
 				IndexView<Subject> i = new IndexView<Subject>(abc, Subject.class, c);
 				i.setOnItemPickedListener(pickedListener);
 				index = i;
 			} else if (viewId == R.id.homework) {
 				title = "Homework";
 				HomeworkController c = new HomeworkController(abc, abc.db(), slot.subject_id);
 				index = new IndexView<Homework>(abc, Homework.class, c);
 			} else if (viewId == R.id.grades) {
 				title = "Grades";
 				GradeController c = new GradeController(abc, abc.db(), slot.subject_id);
 				index = new IndexView<Grade>(abc, Grade.class, c);
 			} else if (viewId == R.id.teacher) {
 				title = "Teacher";
 				TeacherController c = new TeacherController(abc, abc.db());
 				IndexView<Teacher> i = new IndexView<Teacher>(abc, Teacher.class, c);
 				i.setOnItemPickedListener(teacherPickedListener);
 				index = i;
 			} else {
 				throw new IllegalArgumentException();
 			}
 			
 			index.setTitle(title);
 			index.setFlipper(flipper);
 			
 			NavigateBackView.Item item = new NavigateBackView.Item(getContext());
 			item.opener = SlotDetailView.this;
 			item.view = index;
 			
 			flipper.showView(item);
 		}
 	};
 	
 	// This is the right flipper
 	private HistoryViewFlipper flipper;
 	
 	private ViewGroup group(int id) {
 		return (ViewGroup) findViewById(id);
 	}
 	
 	private ViewGroup setText(int id, String text) {
 		ViewGroup layout = group(id);
 		if (text == null)
 			text = getResources().getString(R.string.edit);
 		((TextView) layout.findViewById(R.id.content)).setText(text);
 		return layout;
 	}
 	
 	private AbbecedarioActivity abc;
 	
 	private IndexView.OnItemPickedListener<Teacher> teacherPickedListener = new IndexView.OnItemPickedListener<Teacher>() {
 
 		@Override
 		public void onItemPicked(Teacher teacher) {
 			flipper.back();
 			slot.teacher_id = teacher._id;
 			slot.save(abc.db());
 			fillView(slot);
 		}
 	};
 }
