 package com.sk.methods.action;
 
 import java.util.Collections;
 import java.util.EnumSet;
 
 import org.powerbot.script.lang.AbstractQuery;
 import org.powerbot.script.lang.Filter;
import org.powerbot.script.lang.Identifiable;
 import org.powerbot.script.wrappers.Action.Type;
 
 import com.sk.SkMethodContext;
 import com.sk.methods.action.ability.AbilityLevel;
 import com.sk.methods.action.ability.AbilityStyle;
 import com.sk.methods.action.magic.Spellbook;
 import com.sk.methods.action.structure.BarIcon;
 
 public abstract class ActionQuery<T extends Action> extends AbstractQuery<ActionQuery<T>, T> implements
 		Identifiable.Query<ActionQuery<T>> {
 
 	public SkMethodContext ctx;
 
 	public ActionQuery(SkMethodContext ctx) {
 		super(ctx);
 		this.ctx = ctx;
 	}
 
 	public ActionQuery<T> icon(final BarIcon... icon) {
 		return select(new Filter<T>() {
 			@Override
 			public boolean accept(T t) {
 				BarIcon v = t.getBarIcon();
 				if (!v.isValid())
 					return false;
 				for (BarIcon i : icon)
 					if (v.equals(i))
 						return true;
 				return false;
 			}
 		});
 	}
 
 	public ActionQuery<T> book(final EnumSet<Spellbook> book) {
 		return select(new Filter<T>() {
 			@Override
 			public boolean accept(T t) {
 				return book.contains(t.getSpell().getSpellbook());
 			}
 		});
 	}
 
 	public ActionQuery<T> book(Spellbook... books) {
 		EnumSet<Spellbook> search = EnumSet.noneOf(Spellbook.class);
 		Collections.addAll(search, books);
 		return book(search);
 	}
 
 	public ActionQuery<T> level(final EnumSet<AbilityLevel> levels) {
 		return select(new Filter<T>() {
 			@Override
 			public boolean accept(T t) {
 				return levels.contains(t.getAbility().getAbilityLevel());
 			}
 		});
 	}
 
 	public ActionQuery<T> level(final AbilityLevel... level) {
 		EnumSet<AbilityLevel> search = EnumSet.noneOf(AbilityLevel.class);
 		Collections.addAll(search, level);
 		return level(search);
 	}
 
 	public ActionQuery<T> style(final EnumSet<AbilityStyle> styles) {
 		return select(new Filter<T>() {
 			@Override
 			public boolean accept(T t) {
 				return styles.contains(t.getAbility().getStyle());
 			}
 		});
 	}
 
 	public ActionQuery<T> style(final AbilityStyle... style) {
 		EnumSet<AbilityStyle> search = EnumSet.noneOf(AbilityStyle.class);
 		Collections.addAll(search, style);
 		return style(search);
 	}
 
 	public ActionQuery<T> validAbility() {
 		return select(new Filter<T>() {
 			@Override
 			public boolean accept(T t) {
 				return t.getAbility().isValid();
 			}
 		});
 	}
 
 	public ActionQuery<T> ready() {
 		return select(new Filter<T>() {
 			@Override
 			public boolean accept(T t) {
 				return t.isReady();
 			}
 		});
 	}
 
 	public ActionQuery<T> type(final Type type) {
 		return select(new Filter<T>() {
 			@Override
 			public boolean accept(T t) {
 				return t.getType() == type;
 			}
 		});
 	}
 
 	public <K extends BarIcon> ActionQuery<T> iconType(final Class<K> clazz) {
 		return select(new Filter<T>() {
 			@Override
 			public boolean accept(T t) {
 				return clazz.isInstance(t);
 			}
 		});
 	}
 
 	@Override
 	public ActionQuery<T> id(Identifiable... arr) {
 		return select(new Identifiable.Matcher(arr));
 	}
 
 	@Override
 	public ActionQuery<T> id(int[]... arrs) {
 		int len = 0;
 		for (int[] is : arrs)
 			len += is.length;
 		int[] joined = new int[len];
 		int curPos = 0;
 		for (int[] is : arrs) {
 			System.arraycopy(is, 0, joined, curPos, is.length);
 			curPos += is.length;
 		}
 		return id(joined);
 	}
 
 	@Override
 	public ActionQuery<T> id(int... ids) {
 		return select(new Identifiable.Matcher(ids));
 	}
 
 	@Override
 	protected ActionQuery<T> getThis() {
 		return this;
 	}
 
 }
