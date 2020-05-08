 package de.objectcode.time4u.client.ui.views;
 
 import java.util.Calendar;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.GroupMarker;
 import org.eclipse.jface.action.IMenuListener;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.FontData;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.ui.IWorkbenchActionConstants;
 import org.eclipse.ui.part.ViewPart;
 import org.vafada.swtcalendar.SWTCalendar;
 import org.vafada.swtcalendar.SWTCalendarEvent;
 import org.vafada.swtcalendar.SWTCalendarListener;
 
 import de.objectcode.time4u.client.store.api.IWorkItemRepository;
 import de.objectcode.time4u.client.store.api.RepositoryFactory;
 import de.objectcode.time4u.client.store.api.event.IRepositoryListener;
 import de.objectcode.time4u.client.store.api.event.RepositoryEvent;
 import de.objectcode.time4u.client.store.api.event.RepositoryEventType;
 import de.objectcode.time4u.client.ui.UIPlugin;
 import de.objectcode.time4u.client.ui.provider.DayFontColorProvider;
 import de.objectcode.time4u.client.ui.util.CompoundSelectionEntityType;
 import de.objectcode.time4u.client.ui.util.CompoundSelectionProvider;
 import de.objectcode.time4u.client.ui.util.SelectionServiceAdapter;
 import de.objectcode.time4u.server.api.data.CalendarDay;
 import de.objectcode.time4u.server.api.data.DayInfo;
 import de.objectcode.time4u.server.api.data.DayTag;
 
 public class CalendarView extends ViewPart implements SWTCalendarListener, IRepositoryListener
 {
   public static final String ID = "de.objectcode.time4u.client.ui.view.calendarView";
 
   SWTCalendar m_calendar;
 
   int m_currentMonth;
   int m_currentYear;
 
   int m_refreshCounter = 0;
 
   private CompoundSelectionProvider m_selectionProvider;
 
   Font m_boldFont;
   Font m_italicFont;
 
   /**
    * This is a callback that will allow us to create the viewer and initialize it.
    */
   @Override
   public void createPartControl(final Composite parent)
   {
     m_selectionProvider = new CompoundSelectionProvider();
     getSite().setSelectionProvider(m_selectionProvider);
     getSite().getPage().addSelectionListener(m_selectionProvider);
 
     m_calendar = new SWTCalendar(parent, SWTCalendar.SHOW_WEEK_NUMBERS);
     m_calendar.addSWTCalendarListener(this);
 
     final CalendarDay selection = (CalendarDay) m_selectionProvider
         .getSelection(CompoundSelectionEntityType.CALENDARDAY);
     Assert.isNotNull(selection);
 
     m_currentMonth = selection.getMonth();
     m_currentYear = selection.getYear();
     m_calendar.setCalendar(selection.getCalendar());
 
     final Font originalFont = m_calendar.getFont();
     final FontData boldFontData[] = originalFont.getFontData();
     final FontData italicFontData[] = originalFont.getFontData();
 
     // Adding the bold attribute
     for (int i = 0; i < boldFontData.length; i++) {
       boldFontData[i].setStyle(boldFontData[i].getStyle() | SWT.BOLD);
     }
     for (int i = 0; i < italicFontData.length; i++) {
       italicFontData[i].setStyle(italicFontData[i].getStyle() | SWT.ITALIC);
     }
 
     m_boldFont = new Font(m_calendar.getDisplay(), boldFontData);
     m_italicFont = new Font(m_calendar.getDisplay(), italicFontData);
 
     try {
       final IWorkItemRepository workItemRepository = RepositoryFactory.getRepository().getWorkItemRepository();
 
       final DayFontColorProvider provider = new DayFontColorProvider(m_calendar.getBackground(), m_calendar
           .getForeground(), m_calendar.getFont(), m_boldFont, m_italicFont, workItemRepository, m_currentYear,
           m_currentMonth);
 
       m_calendar.setColorProvider(provider);
       m_calendar.setFontProvider(provider);
     } catch (final Exception e) {
       UIPlugin.getDefault().log(e);
     }
 
     final MenuManager menuMgr = new MenuManager();
 
     menuMgr.setRemoveAllWhenShown(true);
     menuMgr.addMenuListener(new IMenuListener() {
       public void menuAboutToShow(final IMenuManager manager)
       {
         menuMgr.add(new GroupMarker("calendarGroup"));
         menuMgr.add(new Separator());
         menuMgr.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
         final CalendarDay selection = (CalendarDay) m_selectionProvider
             .getSelection(CompoundSelectionEntityType.CALENDARDAY);
         Assert.isNotNull(selection);
 
         try {
           final DayInfo dayInfo = RepositoryFactory.getRepository().getWorkItemRepository().getDayInfo(selection);
           final List<DayTag> dayTags = RepositoryFactory.getRepository().getWorkItemRepository().getDayTags();
 
           final Set<String> currentTags = dayInfo != null ? dayInfo.getTags() : new HashSet<String>();
          final int regularTime = dayInfo.getRegularTime();
 
           if (!dayTags.isEmpty()) {
             menuMgr.add(new Separator());
             for (final DayTag dayTag : dayTags) {
               menuMgr.add(new SetDayTagAction(selection, regularTime, currentTags, dayTag));
             }
           }
         } catch (final Exception e) {
           UIPlugin.getDefault().log(e);
         }
 
       }
     });
 
     final Menu menu = menuMgr.createContextMenu(m_calendar);
 
     m_calendar.setMenu(menu);
 
     getSite().registerContextMenu(menuMgr, new SelectionServiceAdapter(getSite().getPage()));
 
     RepositoryFactory.getRepository().addRepositoryListener(RepositoryEventType.DAYINFO, this);
     RepositoryFactory.getRepository().addRepositoryListener(RepositoryEventType.TIMEPOLICY, this);
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   public void setFocus()
   {
     m_calendar.setFocus();
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   public void dispose()
   {
     RepositoryFactory.getRepository().removeRepositoryListener(RepositoryEventType.DAYINFO, this);
     RepositoryFactory.getRepository().removeRepositoryListener(RepositoryEventType.TIMEPOLICY, this);
 
     super.dispose();
   }
 
   public void refresh()
   {
     try {
       final IWorkItemRepository workItemRepository = RepositoryFactory.getRepository().getWorkItemRepository();
       final DayFontColorProvider provider = new DayFontColorProvider(m_calendar.getBackground(), m_calendar
           .getForeground(), m_calendar.getFont(), m_boldFont, m_italicFont, workItemRepository, m_currentYear,
           m_currentMonth);
 
       m_calendar.setColorProvider(provider);
       m_calendar.setFontProvider(provider);
     } catch (final Exception e) {
       UIPlugin.getDefault().log(e);
     }
   }
 
   /**
    * {@inheritDoc}
    */
   public void dateChanged(final SWTCalendarEvent event)
   {
     final Calendar calendar = event.getCalendar();
 
     if (calendar.get(Calendar.MONTH) + 1 != m_currentMonth || calendar.get(Calendar.YEAR) != m_currentYear) {
       m_currentMonth = calendar.get(Calendar.MONTH) + 1;
       m_currentYear = calendar.get(Calendar.YEAR);
 
       refresh();
     }
 
     m_selectionProvider.changeSelection(CompoundSelectionEntityType.CALENDARDAY, new CalendarDay(calendar));
   }
 
   /**
    * {@inheritDoc}
    */
   public void handleRepositoryEvent(final RepositoryEvent event)
   {
     // It's not necessary to queue more than 2 refreshes
 
     switch (event.getEventType()) {
       case DAYINFO:
       case TIMEPOLICY:
         synchronized (this) {
           if (m_refreshCounter >= 2) {
             return;
           }
 
           m_refreshCounter++;
         }
         m_calendar.getDisplay().asyncExec(new Runnable() {
           public void run()
           {
             try {
               refresh();
             } finally {
               synchronized (CalendarView.this) {
                 m_refreshCounter--;
               }
             }
           }
         });
         break;
     }
   }
 
   static class SetDayTagAction extends Action
   {
     CalendarDay m_currentDay;
     Set<String> m_currentTags;
     DayTag m_dayTag;
 
     SetDayTagAction(final CalendarDay currentDay, final int regularTime, final Set<String> currentTags,
         final DayTag dayTag)
     {
       super(dayTag.getName(), Action.AS_CHECK_BOX);
 
       setText(dayTag.getName());
       setToolTipText(dayTag.getDescription());
 
       m_currentDay = currentDay;
       m_currentTags = currentTags;
       m_dayTag = dayTag;
 
       setChecked(m_currentTags.contains(m_dayTag.getName()));
 
       if (!currentTags.isEmpty()) {
         setEnabled(currentTags.contains(dayTag.getName()) || dayTag.getRegularTime() == null
             || dayTag.getRegularTime() == regularTime);
       }
     }
 
     @Override
     public void run()
     {
       if (m_currentTags.contains(m_dayTag.getName())) {
         m_currentTags.remove(m_dayTag.getName());
       } else {
         m_currentTags.add(m_dayTag.getName());
       }
 
       try {
         RepositoryFactory.getRepository().getWorkItemRepository().setRegularTime(m_currentDay, m_currentDay,
             m_dayTag.getRegularTime(), m_currentTags);
       } catch (final Exception e) {
         UIPlugin.getDefault().log(e);
       }
     }
   }
 }
