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
 
 package elw.web.core;
 
 import com.google.common.base.Strings;
 import elw.dao.Ctx;
 import elw.dao.QueriesImpl;
 import elw.vo.*;
 import elw.vo.Class;
 import elw.web.ElwUri;
 import elw.web.VelocityTemplates;
 import elw.web.VtTuple;
 
 import java.util.*;
 
 public class Core {
     private static final String CTX_TO_SCORE_TOTAL = "--total";
 
     private final QueriesImpl queries;
 
     private final VelocityTemplates vt = VelocityTemplates.INSTANCE;
     private final ElwUri uri = new ElwUri();
 
     public Core(QueriesImpl queries) {
         this.queries = queries;
     }
 
     public VelocityTemplates getTemplates() {
         return vt;
     }
 
     public ElwUri getUri() {
         return uri;
     }
 
     public QueriesImpl getQueries() {
         return queries;
     }
 
     public List<Object[]> log(
             Ctx ctx, Format format, LogFilter logFilter, final boolean adm
     ) {
         final List<Object[]> logData = new ArrayList<Object[]>();
 
         if ("s".equalsIgnoreCase(logFilter.getScopePath()[0])) {
             logStud(ctx, format, logFilter, logData, adm);
         } else if ("c".equalsIgnoreCase(logFilter.getScopePath()[0])) {
             logCourse(ctx, format, logFilter, logData, adm);
         }
 
         return logData;
     }
 
     private void logCourse(
             Ctx ctx, Format format, LogFilter lf,
             List<Object[]> logData, final boolean adm
     ) {
         for (IndexEntry indexEntry : ctx.getEnr().getIndex().values()) {
             final Ctx ctxAss = ctx.extendIndex(indexEntry.getId());
 
             final TaskType aType = ctxAss.getAssType();
             for (FileSlot slot : aType.getFileSlots().values()) {
                 if (W.excluded(lf.getSlotId(), aType.getId(), slot.getId())) {
                     continue;
                 }
                 if (!lf.cDue(ctxAss, slot)) {
                     continue;
                 }
                 if (!adm && !ctxAss.cFrom().isStarted()) {
                     continue;
                 }
 
                 final List<Version> versions;
                 if (adm) {
                     versions = new ArrayList<Version>(ctxAss.getAss().getVersions().values());
                 } else {
                     //  FIXME also shared versions should be added here
                     versions = Collections.singletonList(ctxAss.getVer());
                 }
                 int total = 0;
                 for (Version ver : versions) {
                     final Ctx ctxVer = ctxAss.extendVer(ver);
                     final List<Attachment> uploadsVer = queries.attachments(ctxVer.ctxSlot(slot.getId()));
                     total += uploadsVer.size();
                     if (lf.cScopeOne('v') && lf.cVer(ctxVer)) {
                         logRows(format, lf, logData, indexEntry.getId(), ctxVer, slot, uploadsVer, Attachment.SCOPE, adm);
                     }
                 }
             }
         }
     }
 
     private void logStud(Ctx ctx, Format f, LogFilter lf, List<Object[]> logData, boolean adm) {
         if (adm) {
             if (lf.getVerId() != null && lf.getVerId().trim().length() > 0 &&
                     (lf.getStudId() == null || lf.getStudId().trim().length() == 0)) {
                 //	do the same, but across all enrollments
                 for (Enrollment enr : queries.enrollments()) {
                     if (enr.getCourseId().equals(ctx.getCourse().getId())) {
                         final Ctx enrCtx = Ctx.forEnr(enr).resolve(queries);
                         for (Student stud : enrCtx.getGroup().getStudents().values()) {
                             if (W.excluded(lf.getStudId(), stud.getId())) {
                                 continue;
                             }
 
                             final Ctx ctxStud = enrCtx.retainAll(Ctx.STATE_ECG).extendStudent(stud);
                             logStudForStud(ctxStud, f, lf, logData, ctxStud, adm);
                         }
                     }
                 }
             } else {
                 for (Student stud : ctx.getGroup().getStudents().values()) {
                     if (W.excluded(lf.getStudId(), stud.getId())) {
                         continue;
                     }
 
                     final Ctx ctxStud = ctx.retainAll(Ctx.STATE_ECG).extendStudent(stud);
                     logStudForStud(ctx, f, lf, logData, ctxStud, adm);
                 }
             }
         } else {
             if (ctx.getStudent() != null) {
                 logStudForStud(ctx, f, lf, logData, ctx, adm);
             }
         }
     }
 
     private void logStudForStud(
             Ctx ctx, Format f,
             LogFilter lf, List<Object[]> logData, Ctx ctxStud,
             boolean adm) {
         for (IndexEntry indexEntry : ctx.getEnr().getIndex().values()) {
             final Ctx ctxVer = ctxStud.extendIndex(indexEntry.getId());
             if (!adm && !ctxVer.cFrom().isStarted()) {
                 continue;
             }
             if (!lf.cVer(ctxVer)) {
                 continue;
             }
             final TaskType aType = ctxVer.getAssType();
 
             for (FileSlot slot : aType.getFileSlots().values()) {
                 if (W.excluded(lf.getSlotId(), aType.getId(), slot.getId())) {
                     continue;
                 }
 
                 final List<Solution> uploads = queries.solutions(ctxVer.ctxSlot(slot));
                 if (!lf.cDue(ctxVer, slot)) {
                     continue;
                 }
 
                 logRows(f, lf, logData, indexEntry.getId(), ctxVer, slot, uploads, Solution.SCOPE, adm);
                 if (uploads.size() == 0 && lf.cScopeStud(slot, null)) {
                     logData.add(logRow(f, lf.getMode(), logData, indexEntry.getId(), ctxVer, slot, null, Solution.SCOPE, adm));
                 }
             }
         }
     }
 
     private int logRows(
             Format format, LogFilter logFilter, List<Object[]> logData, String index, Ctx ctxVer, FileSlot slot,
             List<? extends FileBase> uploads, String scope, boolean adm
     ) {
         int shown = 0;
         for (int i = 0, uploadsLength = uploads.size(); i < uploadsLength; i++) {
             final boolean last = i + 1 == uploadsLength;
             if (!logFilter.cLatest(last)) {
                 continue;
             }
             final FileBase e = uploads.get(i);
             if (Solution.SCOPE.equals(scope) && !logFilter.cScopeStud(slot, e)) {
                 continue;
             }
             shown += 1;
             logData.add(logRow(format, logFilter.getMode(), logData, index, ctxVer, slot, e, scope, adm));
         }
         return shown;
     }
 
     private Object[] logRow(
             Format f, final String mode, List<Object[]> data,
             String index, Ctx ctx, FileSlot slot, FileBase fileBase, String scope,
             boolean adm) {
         final long time = fileBase == null ? System.currentTimeMillis() : fileBase.getStamp();
 
         final String nameNorm;
         if (fileBase == null) {
             nameNorm = "";
         } else {
             if (adm && Solution.SCOPE.equalsIgnoreCase(scope)) {
                 nameNorm = ctx.cmpNameNorm(f, slot, fileBase);
             } else {
                 nameNorm = fileBase.getName();
             }
         }
 
         final SortedMap<String, List<Solution>> stud = queries.solutions(ctx);
         final boolean studEditable =
                 Solution.SCOPE.equals(scope) && ctx.checkWrite(slot, stud);
 
         final String ulRef;
         if (adm) {
             if (!Solution.SCOPE.equals(scope)) {
                 //	FIXME context/scope here is not set properly
                 ulRef = uri.upload(ctx, scope, slot.getId());
             } else {
                 ulRef = null;
             }
         } else {
             if (studEditable) {
                 ulRef = uri.upload(ctx, scope, slot.getId());
             } else {
                 ulRef = null;
             }
         }
 
         String editRef = null;
         if (slot.getFileTypes().size() == 1 && !Strings.isNullOrEmpty(IdNamed._.one(slot.getFileTypes()).getEditor())) {
             if (adm || studEditable) {
                 editRef = uri.edit(ctx, scope, slot.getId(), fileBase == null ? null : fileBase.getId());
             }
         }
         final String authorName;
         if (fileBase == null) {
             if (Solution.SCOPE.equalsIgnoreCase(scope)) {
                 authorName = ctx.getStudent().getName();
             } else {
                 authorName = "Admin";
             }
         } else {
             authorName = fileBase.getAuthor();
         }
 
         final VtTuple status = vt.status(f, mode, scope, ctx, slot, fileBase);
 
         final String nameComment;
         if (fileBase == null) {
             nameComment = "";
         } else {
             if (fileBase.getComment() != null && fileBase.getComment().trim().length() > 0) {
                 nameComment = fileBase.getName() + " / " + fileBase.getComment();
             } else {
                 nameComment = fileBase.getName();
             }
         }
 
         final Object[] dataRow = {
                 /* 0 index */ data.size(),
                 /* 1 upload millis */ time,
                 /* 2 nice date - full */ fileBase == null ? "" : f.format(time, "EEE d HH:mm"),
                 /* 3 nice date - nice */ fileBase == null ? "" : f.format(time),
                 /* 4 author.id */ Solution.SCOPE.equalsIgnoreCase(scope) ? ctx.getStudent().getId() : "admin",
                 /* 5 author.name */ authorName,
                 /* 6 class.index */ index,
                 /* 7 class.name */ ctx.getAss().getName(),
                 /* 8 slot.id */ ctx.getAssType().getId() + "--" + slot.getId(),
                 /* 9 slot.name */ ctx.getVer() != null ? ctx.getVer().getName() + " / " + slot.getName() : slot.getName(),
                 /* 10 comment */ nameComment,
                 /* 11 status sort*/ status.getSort(),
                 /* 12 status text*/ status.getText(),
                 /* 13 status classes */ status.getClasses(),
                 /* 14 source ip */ fileBase == null ? "" : fileBase.getSourceAddress(),
                 /* 15 size bytes */ fileBase == null ? "" : fileBase.computeSize(),
                 /* 16 size */ fileBase == null ? "" : f.formatSize(fileBase.computeSize()),
                 /* 17 approve ref */ adm && Solution.SCOPE.equals(scope) ? uri.approve(ctx, scope, slot.getId(), fileBase) : null,
                 /* 18 dl ref */ uri.download(ctx, scope, slot.getId(), fileBase, nameNorm),
                 /* 19 ul ref */ ulRef,
                 /* 20 edit ref */ editRef,
                 /* 21 comment ref */ adm ? null : "#"    //	TODO comment edit url/page/method
         };
         return dataRow;
     }
 
     public List<Object[]> index(List<Enrollment> enrolls) {
         final List<Object[]> indexData = new ArrayList<Object[]>();
         for (Enrollment enr : enrolls) {
             indexData.add(indexRow(indexData, enr));
         }
         return indexData;
     }
 
     private Object[] indexRow(List<Object[]> indexData, Enrollment enr) {
         final Group group = queries.group(enr.getGroupId());
         final Course course = queries.course(enr.getCourseId());
 
         final Object[] arr = {
                 /* 0 index - */ indexData.size(),
                 /* 1 enr.id - */ enr.getId(),
                 /* 2 group.id - */ group.getId(),
                 /* 3 group.name 0*/ group.getName(),
                 /* 4 course.id - */ course.getId(),
                 /* 5 course.name 1 */ course.getName(),
                 /* 6 summary ref 2 */ uri.summary(enr.getId()),
                 /* 7 students ref 3 */ "#",
                 /* 8 tasks ref 4 */ uri.tasks(enr.getId()),
                 /* 9 classes ref 5 */ "#",
                 /* 10 uploads ref 6 */ uri.logPendingE(enr.getId()),
                 /* 11 uploads-open ref 7 */ uri.logOpenE(enr.getId()),
                 /* 12 uploads-course ref 8 */ uri.logCourseE(enr.getId()),
         };
         return arr;
     }
 
     public List<Object[]> logScore(
             SortedMap<Long, Score> allScores, Ctx ctx, FileSlot slot, Solution file,
             Format f, String mode, Long stamp
     ) {
         final List<Object[]> logData = new ArrayList<Object[]>();
 
         final Score scoreBest = chooseBestScore(allScores, ctx, slot);
 
         for (Long s : allScores.keySet()) {
             final Score scoreEntry = allScores.get(s);
 
             final Long createStamp = scoreEntry.getStamp();
             final boolean selected = stamp == null ? createStamp == null : stamp.equals(createStamp);
 
             final long time;
             String approveUri = "approve?elw_ctx=" + ctx.toString() + "&sId=" + slot.getId() + "&fId=" + file.getId();
             if (createStamp == null) {
                 time = System.currentTimeMillis();
             } else {
                 time = createStamp;
                 approveUri += "&stamp=" + createStamp.toString();
             }
 
             final VtTuple status = vt.status(f, mode, Solution.SCOPE, ctx, slot, file, scoreEntry);
             final VtTuple statusScoring = vt.status(f, "s", Solution.SCOPE, ctx, slot, file, scoreEntry);
 
             final Object[] logRow = new Object[]{
                     /* 0 index - */ logData.size(),
                     /* 1 selected 0 */ selected ? "&gt;" : "",
                     /* 2 best 1 */ scoreBest == scoreEntry ? "*" : "",
                     /* 3 score date millis - */ time,
                     /* 4 score date full - */ f.format(time, "EEE d HH:mm"),
                     /* 5 score date nice 2 */ f.format(time),
                     /* 6 status classes - */ status.getClasses(),
                     /* 7 status text 3 */ status.getText(),
                     /* 8 scoring 4 */ statusScoring.getText(),
                     /* 9 comment 5 */ scoreEntry.getComment(),
                     /* 10 edit score 6 */ approveUri
             };
             logData.add(logRow);
         }
 
         return logData;
     }
 
     private Score chooseBestScore(SortedMap<Long, Score> allScores, Ctx ctx, FileSlot slot) {
         Score scoreBest = null;
         double pointsBest = 0;
         for (Long s : allScores.keySet()) {
             final Score scoreCur = allScores.get(s);
             final double pointsCur = ctx.getIndexEntry().computePoints(scoreCur, slot);
             if (scoreBest == null || pointsBest < pointsCur) {
                 scoreBest = scoreCur;
                 pointsBest = pointsCur;
             }
         }
         return scoreBest;
     }
 
     public List<Object[]> tasks(Ctx ctx, final LogFilter filter, Format f, boolean adm) {
         final List<Object[]> indexData = new ArrayList<Object[]>();
 
         final SortedMap<String, Map<String, List<Solution>>> ctxVerToSlotToFiles =
                 new TreeMap<String, Map<String, List<Solution>>>();
         final SortedMap<String, Double> ctxEsToScore = new TreeMap<String, Double>();
         final SortedMap<String, Summary> ctxEsToSummary = new TreeMap<String, Summary>();
 
         final int studCount = tasksData(ctx, filter, adm, ctxVerToSlotToFiles, ctxEsToScore, ctxEsToSummary);
 
         int totalBudget = 0;
         for (IndexEntry indexEntry : ctx.getEnr().getIndex().values()) {
             final Ctx ctxAss = ctx.extendIndex(indexEntry.getId());
             indexData.add(tasksRow(f, indexData, ctxAss, adm, ctxEsToScore, ctxEsToSummary, studCount));
             totalBudget += ctxAss.getIndexEntry().getScoreBudget();
         }
 
         final Double totalScore = ctxEsToScore.get(CTX_TO_SCORE_TOTAL);
         if (totalBudget > 0 && studCount > 0 && totalScore != null) {
             final double avgScore = totalScore / studCount;
             final String score = f.format2(avgScore) + " of " + totalBudget + ": " + vt.niceRatio(f, avgScore / totalBudget, "");
             indexData.add(new Object[]{
                     /* 0 index - */ indexData.size(),
                     /* 1 date millis - */ 0,
                     /* 2 date full - */ "",
                     /* 3 date nice 0*/ "",
                     /* 4 tType.id - */ "",
                     /* 5 tType.name 1 */ "",
                     /* 6 task.id ref - */ "",
                     /* 7 task.name ref 2 */ "",
                     /* 8 summary status sort - */ 0,
                     /* 9 summary status text - */ "",
                     /* 10 summary status text 3 */ "",
                     /* 11 summary due millis - */ 0,
                     /* 12 summary due full - */ "",
                     /* 13 summary due nice 4 */ "<b>Total:</b>",
                     /* 14 score sort */ 0,
                     /* 15 score nice 5 */ score,
                     /* 16 uploads ref 6 */ null,
                     /* 17 uploads-open ref 7 */ null,
                     /* 18 uploads-course ref 8 */ null,
                     /* 19 task-total sort - */ 1
             });
         }
 
         return indexData;
     }
 
     private Object[] tasksRow(
             Format f, List<Object[]> indexData, Ctx ctxAss, boolean adm,
             SortedMap<String, Double> ctxEsToScore, SortedMap<String, Summary> ctxEsToSummary, int studCount
     ) {
         final Class classFrom = ctxAss.cFrom();
 
         final VtTuple summary = vt.summary(ctxAss, ctxEsToSummary, studCount);
         final Summary summ = ctxEsToSummary.get(ctxAss.ei());
 
         final String dueNice;
         final String dueFull;
         final long dueSort;
         if (classFrom.isStarted()) {
             if (summ.getEarliestDue() == null) {
                 dueNice = "None";
                 dueFull = "";
                 dueSort = System.currentTimeMillis();
             } else {
                 dueNice = "Due " + f.format(summ.getEarliestDue());
                 dueFull = f.format(summ.getEarliestDue(), "EEE d HH:mm");
                 dueSort = summ.getEarliestDue();
             }
         } else {
             dueNice = "Opens " + f.format(classFrom.getFromDateTime().getMillis());
             dueFull = f.format(classFrom.getFromDateTime().getMillis(), "EEE d HH:mm");
             dueSort = classFrom.getFromDateTime().getMillis();
         }
 
         final String scoreNice;
         final double scoreSort;
         if (ctxEsToScore.get(ctxAss.ei()) != null) {
             final int budget = ctxAss.getIndexEntry().getScoreBudget();
             if (budget > 0) {
                 final double score = ctxEsToScore.get(ctxAss.ei()) / studCount;
                 scoreNice = f.format2(score) + " of " + budget + ": " + vt.niceRatio(f, score / budget, "");
                 scoreSort = score;
             } else {
                 scoreSort = -1;
                 scoreNice = "";
             }
         } else {
             scoreNice = "?";
             scoreSort = -2;
         }
 
         final Object[] arr = {
                 /* 0 index - */ indexData.size(),
                 /* 1 date millis - */ classFrom.getFromDateTime().getMillis(),
                 /* 2 date full - */ f.format(classFrom.getFromDateTime().getMillis(), "EEE d HH:mm"),
                 /* 3 date nice 0*/ f.format(classFrom.getFromDateTime().getMillis()),
                 /* 4 tType.id - */ ctxAss.getAssType().getId(),
                 /* 5 tType.name 1 */ ctxAss.getAssType().getName(),
                 /* 6 task.id ref - */ ctxAss.getAss().getId(),
                 /* 7 task.name ref 2 */ ctxAss.getAss().getName(),
                 /* 8 summary status sort - */ summary.getSort(),
                 /* 9 summary status text - */ summary.getClasses(),
                 /* 10 summary status text 3 */ summary.getText(),
                 /* 11 summary due millis - */ dueSort,
                 /* 12 summary due full - */ dueFull,
                 /* 13 summary due nice 4 */ dueNice,
                 /* 14 score sort */ scoreSort,
                 /* 15 score nice 5 */ scoreNice,
                 /* 16 uploads ref 6 */ adm ? uri.logPendingEA(ctxAss) : classFrom.isStarted() ? uri.logPendingEAV(ctxAss) : null,
                 /* 17 uploads-open ref 7 */ adm ? uri.logOpenEA(ctxAss) : classFrom.isStarted() ? uri.logOpenEAV(ctxAss) : null,
                 /* 18 uploads-course ref 8 */ adm ? uri.logCourseEA(ctxAss) : classFrom.isStarted() ? uri.logCourseEAV(ctxAss) : null,
                 /* 19 task-total sort - */ 0
         };
 
         return arr;
     }
 
     /**
      * Generate per-task data score summaries
      *
      * @param ctxEnr       context with a student set (non-adm) or only enrollment set (adm)
      * @param filter       to filter tasks and/or students
      * @param adm          whether this is an admin report or not
      * @param fileMetas    to store per slot file meta listings
      * @param ctxToScore   to store totals per task
      * @param ctxToSummary to handle open/pending/approved stats
      * @return number of students processed in this report
      */
     public int tasksData(
             Ctx ctxEnr, LogFilter filter, boolean adm,
             Map<String, Map<String, List<Solution>>> fileMetas,
             Map<String, Double> ctxToScore,
             Map<String, Summary> ctxToSummary
     ) {
         int students = 0;
         if (adm) {
             for (Student stud : ctxEnr.getGroup().getStudents().values()) {
                 if (W.excluded(filter.getStudId(), stud.getId())) {
                     continue;
                 }
                 students += 1;
                 storeTasksData(ctxEnr.extendStudent(stud), filter, fileMetas, ctxToScore, ctxToSummary);
             }
         } else {
             //	let's hope that student id is already present here...
             students += 1;
             storeTasksData(ctxEnr, filter, fileMetas, ctxToScore, ctxToSummary);
         }
 
         return students;
     }
 
     private void storeTasksData(
             Ctx ctxStud, LogFilter filter,
             Map<String, Map<String, List<Solution>>> fileMetas,
             Map<String, Double> ctxToScore,
             Map<String, Summary> ctxToSummary
     ) {
         for (IndexEntry indexEntry : ctxStud.getEnr().getIndex().values()) {
             storeTaskData(ctxStud.extendIndex(indexEntry.getId()), filter, fileMetas, ctxToScore, ctxToSummary);
         }
     }
 
     private void storeTaskData(
             Ctx ctxVer, LogFilter filter,
             Map<String, Map<String, List<Solution>>> fileMetas,
             Map<String, Double> ctxToScore,
             Map<String, Summary> ctxToSummary
     ) {
         final String assPath = ctxVer.toString();
         final TaskType assType = ctxVer.getAssType();
 
         final SortedMap<String, List<Solution>> slotIdToFiles = queries.solutions(ctxVer);
         if (fileMetas != null) {
             fileMetas.put(assPath, slotIdToFiles);
         }
 
         for (FileSlot slot : assType.getFileSlots().values()) {
             if (W.excluded(filter.getSlotId(), assType.getId(), slot.getId())) {
                 continue;
             }
 
             final Class classDue = ctxVer.cDue(slot.getId());
            final Long classDueStamp = classDue != null ? classDue.getToDateTime().getMillis() : null;
             final List<Solution> filesForSlot = slotIdToFiles.get(slot.getId());
             final Solution bestFile = selectBestFile(ctxVer, slot.getId(), filesForSlot, slot);
 
             final double scoreForIdx;
             final Summary sum;
             if (bestFile != null) {
                 final Score score = bestFile.getScore();
 
                 if (score != null && Boolean.TRUE.equals(score.getApproved())) {
                     scoreForIdx = ctxVer.getIndexEntry().computePoints(score, slot);
                 } else {
                     scoreForIdx = 0;
                 }
                 sum = Summary.forScore(classDueStamp, score == null ? null : score.getApproved());
             } else {
                 scoreForIdx = 0;
                 if (classDue == null) {
                     sum = new Summary(0, 0, 0, 0, null);
                 } else if (filesForSlot == null || filesForSlot.isEmpty()) {
                     sum = new Summary(0, 0, 1, 0, classDueStamp);
                 } else {
                     final Solution lastFile = filesForSlot.get(filesForSlot.size() - 1);
                     final Score score = lastFile.getScore();
 
                     sum = Summary.forScore(classDueStamp, score == null ? null : score.getApproved());
                 }
             }
 
             ctxToScore.put(ctxVer.toString(), scoreForIdx);
             Summary.increment(ctxToScore, ctxVer.ei(), scoreForIdx);
             Summary.increment(ctxToScore, ctxVer.es(), scoreForIdx);
             Summary.increment(ctxToScore, CTX_TO_SCORE_TOTAL, scoreForIdx);
             Summary.increment(ctxToSummary, ctxVer.ei(), sum);
         }
     }
 
     private Solution selectBestFile(Ctx ctxVer, String slotId, List<Solution> filesForSlot, final FileSlot slot) {
         if (filesForSlot == null || filesForSlot.isEmpty()) {
             return null;
         }
 
         Solution usedEntry = null;
         double maxScore = 0;
         for (Solution e : filesForSlot) {
             final Score score = e.getScore();
             final double eScore = ctxVer.getIndexEntry().computePoints(score, slot);
 
             if (Boolean.FALSE.equals(score.getApproved())) {
                 continue;
             }
 
             final boolean firstScore = usedEntry == null;
             final boolean betterScore = maxScore < eScore;
             final boolean sameButApproved = maxScore == eScore && Boolean.TRUE.equals(score.getApproved());
             if ((firstScore || betterScore || sameButApproved)) {
                 maxScore = eScore;
                 usedEntry = e;
             }
         }
 
         if (usedEntry != null) {
             final Score s = usedEntry.getScore();
             if (s != null) {
                 s.setBest(true);
             }
         }
 
         return usedEntry;
     }
 
     public String cmpForwardToEarliestPendingSince(Ctx ctx, FileSlot slot, Long since) {
         Solution epF = null;    //	earliest pending
         Ctx epCtx = null;
 
         final Ctx ctxEnr = Ctx.forEnr(ctx.getEnr()).resolve(queries);
         //	LATER oh this pretty obviously looks like we REALLY need some rdbms from now on... :D
         for (Student stud : ctx.getGroup().getStudents().values()) {
             final Ctx ctxStud = ctxEnr.extendStudent(stud);
             for (IndexEntry indexEntry : ctx.getEnr().getIndex().values()) {
                 final Ctx ctxVer = ctxStud.extendIndex(indexEntry.getId());
                 if (!ctxVer.getAssType().getId().equals(ctx.getAssType().getId())) {
                     continue;    //	other ass types out of scope
                 }
                 for (FileSlot s : ctxVer.getAssType().getFileSlots().values()) {
                     if (!s.getId().equals(slot.getId())) {
                         continue;    //	other slots out of scope
                     }
                     final List<Solution> uploads = queries.solutions(ctxVer.ctxSlot(slot));
                     if (uploads != null && uploads.size() > 0) {
                         for (int i = uploads.size() - 1; i >= 0; i--) {
                             final Solution f = uploads.get(i);
                             if (since != null && f.getStamp().compareTo(since) <= 0) {
                                 break;    //	oh this is overly stale
                             }
 
                             if (f.getScore() == null || f.getScore().getApproved() == null) {
                                 if ((epF == null || epF.getStamp() > f.getStamp())) {
                                     epF = f;
                                     epCtx = ctxVer;
                                 }
                                 break;    //	don't look into earlier pending versions before this one is approved
                             } else if (f.getScore().state() == State.APPROVED) {
                                 break;    //	don't look into earlier pending versions after this one is approved
                             }
                         }
                     }
                 }
             }
         }
 
         final String forward;
         if (epCtx != null) {
             forward = "approve?elw_ctx=" + epCtx.toString() + "&sId=" + slot.getId() + "&fId=" + epF.getId();
         } else {
             forward = "log?elw_ctx=" + ctxEnr.toString() + "&f_slot=" + slot.getId() + "&f_scope=s--p--";
         }
         return forward;
     }
 }
