 /*
  * ELW : e-learning workspace
  * Copyright (C) 2010  Anton Kraievoy
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package elw.web;
 
 import elw.dao.Ctx;
 import elw.vo.*;
 import elw.vo.Class;
 import elw.web.core.Summary;
 import org.joda.time.DateTime;
 
 import java.util.List;
 import java.util.SortedMap;
 
 public class VelocityTemplates {
     public static final VelocityTemplates INSTANCE = new VelocityTemplates();
 
     public static final String MODEL_KEY = "elw_vt";
 
     public <E> Ref<E> ref(E e) {
         return new Ref<E>(e);
     }
 
     public Solution bestOrLastOrNull(List<Solution> entries) {
         if (entries == null || entries.isEmpty()) {
             return null;
         }
 
         for (Solution entry : entries) {
             if (entry.getScore() != null && entry.getScore().isBest()) {
                 return entry;
             }
         }
 
         return entries.get(entries.size() - 1);
     }
 
     public VtTuple status(
             Format f, String mode,
             String scope, Ctx ctx, FileSlot slot, FileBase file
     ) {
         return status(f, mode, scope, ctx, slot, file, file instanceof Solution ? ((Solution) file).getScore() : null);
     }
 
     public VtTuple status(
             Format f, String mode,
             String scope, Ctx ctx, FileSlot slot, FileBase file, Score score
     ) {
         final StringBuilder text = new StringBuilder();
         final StringBuilder cls = new StringBuilder();
         final StringBuilder sort = new StringBuilder();
 
         final elw.vo.Class classFrom = ctx.cFrom();
         final elw.vo.Class classDue = ctx.cDue(slot.getId());
 
         if (Solution.SCOPE.equalsIgnoreCase(scope)) {
             if ("a".equalsIgnoreCase(mode)) {
                 if (!classFrom.isStarted()) {
                     text.append("Closed");
                     //				text.append("; Opens ").append(f.format(classFrom.getFromDateTime().getMillis()));
                     cls.append("elw_closed");
                     sort.append(-1);
                 } else if (file == null) {
                     text.append("Open");
                     cls.append("elw_open");
                     sort.append(1);
                 } else if (score == null || score.getApproved() == null) {
                     text.append("Pending");
                     cls.append("elw_pending");
                     sort.append(0);
                 } else if (score.getApproved()) {
                     text.append("Approved");
                     cls.append("elw_approved");
                     sort.append(-2);
                 } else {
                     text.append("Declined");
                     cls.append("elw_declined");
                     sort.append(2);
                 }
             } else if ("d".equalsIgnoreCase(mode)) {
                 if (classDue == null) {
                     text.append("No Due Date");
                     sort.append(0);
                 } else {
                     text.append("Due ").append(f.format(classDue.getToDateTime().getMillis()));
                     sort.append(classDue.getToDateTime().getMillis());
                 }
             } else if ("dd".equalsIgnoreCase(mode)) {
                 if (classDue == null) {
                     text.append("No Due Date");
                     sort.append(0);
                 } else {
                     int dueDiff = classDue.computeToDiffStamp(file);
                     if (dueDiff > 0) {
                         text.append(dueDiff).append("d overdue");
                     } else if (dueDiff == 0) {
                         text.append("Same day");
                     } else {
                         text.append(-dueDiff).append("d ahead");
                     }
                     sort.append(dueDiff);
                 }
             } else if ("p".equalsIgnoreCase(mode)) {
                 if (!classFrom.isStarted()) {
                     text.append("Closed");
                     cls.append("elw_closed");
                     sort.append(0);
                 } else {
                     if (ctx.getIndexEntry().getScoreBudget() > 0) {
                         final double scoreStud;
                         if (file != null && score != null) {
                             scoreStud = ctx.getIndexEntry().computePoints(score, slot);
                         } else {
                             scoreStud = 0;
                         }
                         final double scoreSlot = ctx.getIndexEntry().computePoints(slot);
                         text.append(f.format2(scoreStud)).append(" of ").append(f.format2(scoreSlot));
                         sort.append(scoreStud);
                     } else {
                         sort.append(0);
                     }
 
                     if (file == null) {
                         text.insert(0, "<span class=\"elw_open\">O</span> ");
                        cls.append("elw_open");
                     } else if (score == null || score.getApproved() == null) {
                         final String commentSafe = score == null ? "Preliminary" : score.getComment();
                         text.insert(0, "<span class=\"elw_pending\" title=\"" + f.esc(commentSafe) + "\">P</span> ");
                        cls.append("elw_pending");
                     } else if (score.getApproved()) {
                         text.insert(0, "<span class=\"elw_approved\" title=\"" + f.esc(score.getComment()) + "\">A</span> ");
                        cls.append("elw_approved");
                     } else {
                         text.insert(0, "<span class=\"elw_declined\" title=\"" + f.esc(score.getComment()) + "\">D</span> ");
                        cls.append("elw_declined");
                     }
                 }
             } else if ("s".equalsIgnoreCase(mode)) {
                 if (!classFrom.isStarted()) {
                     text.append("Closed");
                     cls.append("elw_closed");
                     sort.append(0);
                 } else {
                     if (ctx.getIndexEntry().getScoreBudget() > 0) {
                         final double scoreStud;
                         final ScoreTerm[] terms;
                         if (file != null && score != null) {
                             scoreStud = ctx.getIndexEntry().computePoints(score, slot);
                             terms = score.getTerms(ctx.getAssType(), false);
                         } else {
                             scoreStud = 0;
                             terms = new ScoreTerm[0];
                         }
                         sort.append(scoreStud);
                         text.append(f.format2(scoreStud));
                         if (terms.length > 0) {
                             text.append(":");
                             for (ScoreTerm term : terms) {
                                 if (term.getPow() > 1) {
                                     niceRatio(f, term.getRatio(), term.getCriteria().getName() + " x " + term.getPow(), text);
                                 } else {
                                     niceRatio(f, term.getRatio(), term.getCriteria().getName(), text);
                                 }
                             }
                         }
                     } else {
                         sort.append(0);
                     }
 
                     if (file == null) {
                         text.insert(0, "<span class=\"elw_open\">O</span> ");
                        cls.append("elw_open");
                     } else if (score == null || score.getApproved() == null) {
                         final String commentSafe = score == null ? "Preliminary" : score.getComment();
                         text.insert(0, "<span class=\"elw_pending\" title=\"" + f.esc(commentSafe) + "\">P</span> ");
                        cls.append("elw_pending");
                     } else if (score.getApproved()) {
                         text.insert(0, "<span class=\"elw_approved\" title=\"" + f.esc(score.getComment()) + "\">A</span> ");
                        cls.append("elw_approved");
                     } else {
                         text.insert(0, "<span class=\"elw_declined\" title=\"" + f.esc(score.getComment()) + "\">D</span> ");
                        cls.append("elw_declined");
                     }
                 }
             } else if ("dta".equalsIgnoreCase(mode)) {
                 if (!classFrom.isStarted()) {
                     text.append("Closed");
                     cls.append("elw_closed");
                     sort.append(0);
                 } else if (file == null) {
                     text.append("Open");
                     cls.append("elw_open");
                     sort.append(0);
                 } else {
                     final DateTime scoreStamp;
                     if (score == null || score.getStamp() == null) {
                         scoreStamp = new DateTime();
                     } else {
                         scoreStamp = new DateTime(score.getStamp());
                     }
                     final int dta = classFrom.computeToDiff(scoreStamp) - classFrom.computeToDiffStamp(file);
                     text.append(dta).append(" days");
                     sort.append(dta);
                 }
             } else if ("dtu".equalsIgnoreCase(mode)) {
                 if (!classFrom.isStarted()) {
                     text.append("Closed");
                     cls.append("elw_closed");
                     sort.append(0);
                 } else if (file == null) {
                     text.append("Open");
                     cls.append("elw_open");
                     sort.append(0);
                 } else {
                     final int dtu = classFrom.computeToDiffStamp(file);
                     text.append(dtu).append(" days");
                     sort.append(dtu);
                 }
             } else if ("v".equalsIgnoreCase(mode)) {
                 if (!classFrom.isStarted()) {
                     text.append("Closed");
                     cls.append("elw_closed");
                     sort.append(-1);
                 } else {
                     text.append("<span title=\"").append(ctx.getVer().getName()).append("\">").append(ctx.getVer().getId()).append("</span>");
                     sort.append(ctx.getVer().getId());
                 }
             } else if ("ip".equalsIgnoreCase(mode)) {
                 int sortVal = -1;
                 if (file != null) {
                     sortVal++;
                     text.append(file.getSourceAddress());
                     if (!classFrom.checkOnSite(file.getSourceAddress())) {
                         sortVal += 4;
                         cls.append(" elw_offsite");
                     }
                     if (!ctx.getEnr().checkOnTime(file)) {
                         sortVal += 2;
                         cls.append(" elw_offtime");
                     }
                     if (!classFrom.checkOnTime(file)) {
                         sortVal++;
                         cls.append(" elw_rapid");
                     }
                 }
                 sort.append(sortVal);
             }
 
             overdueClasses(file, classDue, cls);
         } else {
             if (file == null) {
                 text.append("Solution");
                 sort.append(-2);
             } else if (Attachment.SCOPE.equalsIgnoreCase(scope)) {
                 text.append("Attachment");
                 sort.append(1);
             }
         }
 
         return new VtTuple(text.toString(), cls.toString(), sort.toString());
     }
 
     private static void overdueClasses(FileBase file, Class classDue, StringBuilder cls) {
         if (classDue != null) {
             if (classDue.isPassed()) {
                 cls.append(" elw_due_passed");
 
                 if (file == null || classDue.computeDaysOverdue(file) > 0) {
                     cls.append(" elw_overdue");
                 }
             }
         } else {
             cls.append(" elw_nodue");
         }
     }
 
     private void niceRatio(Format f, final double ratio, final String title, final StringBuilder sb) {
         if (Math.abs(ratio - 1) < 1e-2) {
             return;
         }
 
         final double percentage = Math.round(ratio * 1000) / 10.0;
 
         final String niceRatio;
         if (percentage < 100) {
             niceRatio = f.format2(100 - percentage) + "%";
         } else {
             niceRatio = f.format2(percentage - 100) + "%";
         }
 
         sb.append(" <span class=\"elw_").append(ratio < 1 ? "neg" : "pos").append("Term\"");
 
         if (title != null && title.trim().length() >= 0) {
             sb.append(" title=\"").append(f.esc(title)).append("\"");
         }
 
         sb.append(">").append(niceRatio).append("</span>");
     }
 
     public String niceRatio(Format f, double ratio, String title) {
         final StringBuilder builder = new StringBuilder();
         niceRatio(f, ratio, title, builder);
         return builder.toString();
     }
 
     public VtTuple summary(Ctx ctxAss, SortedMap<String, Summary> ctxEsToSummary, int studNum) {
         final Summary s = ctxEsToSummary.get(ctxAss.ei());
 
         final StringBuffer text = new StringBuffer();
         if (ctxAss.cFrom().isStarted()) {
             if (s.getDeclinedNum() > 0) {
                 text.append("<span class=\"elw_declined\" title=\"Declined\">").append(s.getDeclinedNum()).append(" D;</span>");
             }
             if (s.getOpenNum() > 0) {
                 text.append("<span class=\"elw_open\" title=\"Open\">").append(s.getOpenNum()).append(" O;</span>");
             }
             if (s.getPendingNum() > 0) {
                 text.append("<span class=\"elw_pending\" title=\"Pending\">").append(s.getPendingNum()).append(" P;</span>");
             }
             if (s.getApprovedNum() > 0) {
                 text.append("<span class=\"elw_approved\" title=\"Approved\">").append(s.getApprovedNum()).append(" A;</span>");
             }
         } else {
             text.append("<span class=\"elw_closed\" title=\"Closed\">").append(s.getDeclinedNum()).append("Closed;</span>");
         }
 
         final long sort = 5 * s.getOpenNum() + 25 * s.getDeclinedNum() - s.getApprovedNum();
 
         return new VtTuple(text.toString(), "", String.valueOf(sort));
     }
 
 }
