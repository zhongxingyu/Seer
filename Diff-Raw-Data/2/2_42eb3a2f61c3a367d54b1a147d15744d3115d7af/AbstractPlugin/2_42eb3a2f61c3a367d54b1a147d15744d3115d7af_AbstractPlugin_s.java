 package jp.ac.osaka_u.ist.sel.metricstool.main.plugin;
 
 
 import java.io.File;
 import java.security.AccessControlException;
 import java.security.Permission;
 import java.security.Permissions;
 import java.util.Enumeration;
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.accessor.ClassInfoAccessor;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.accessor.ClassMetricsRegister;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.accessor.DefaultClassInfoAccessor;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.accessor.DefaultClassMetricsRegister;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.accessor.DefaultFileInfoAccessor;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.accessor.DefaultFileMetricsRegister;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.accessor.DefaultMethodInfoAccessor;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.accessor.DefaultMethodMetricsRegister;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.accessor.FileInfoAccessor;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.accessor.FileMetricsRegister;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.accessor.MethodInfoAccessor;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.accessor.MethodMetricsRegister;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.metric.MetricAlreadyRegisteredException;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.FileInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.MethodInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.io.AlreadyConnectedException;
 import jp.ac.osaka_u.ist.sel.metricstool.main.io.DefaultMessagePrinter;
 import jp.ac.osaka_u.ist.sel.metricstool.main.io.DefaultProgressReporter;
 import jp.ac.osaka_u.ist.sel.metricstool.main.io.MessagePrinter;
 import jp.ac.osaka_u.ist.sel.metricstool.main.io.MessageSource;
 import jp.ac.osaka_u.ist.sel.metricstool.main.io.ProgressReporter;
 import jp.ac.osaka_u.ist.sel.metricstool.main.io.ProgressSource;
 import jp.ac.osaka_u.ist.sel.metricstool.main.io.MessagePrinter.MESSAGE_TYPE;
 import jp.ac.osaka_u.ist.sel.metricstool.main.security.MetricsToolSecurityManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.util.LANGUAGE;
 import jp.ac.osaka_u.ist.sel.metricstool.main.util.METRIC_TYPE;
 
 
 /**
  * gNXvvOCp̒ۃNX
  * <p>
  * evOC͂̃NXpNX1Ȃ΂ȂȂD ܂C̃NXplugin.xmlt@CɎw̌`ŋLqȂ΂ȂȂD
  * <p>
  * mainW[͊evOCfBNgplugin.xmlt@CTA ɋLqĂC̃NXpNXCX^XA
  * e\bhʂď擾Aexecute\bhĂяoăgNXlv
  * 
  * @author kou-tngt
  */
 public abstract class AbstractPlugin implements MessageSource, ProgressSource {
 
     /**
      * vOC̏ۑsσNXD AbstractPlugin̂݃CX^XłD
      * <p>
      * vOC̏𓮓IɕύXƍ̂ŁA̓NX̃CX^Xp Ƃ邱ƂŃvOC̕sϐD
      * evOC̏ۑPluginInfoCX^X̎擾ɂ {@link AbstractPlugin#getPluginInfo()}pD
      * 
      * @author kou-tngt
      * 
      */
     public class PluginInfo {
 
         /**
          * ftHg̃RXgN^
          */
         private PluginInfo() {
             final LANGUAGE[] languages = AbstractPlugin.this.getMeasurableLanguages();
             this.measurableLanguages = new LANGUAGE[languages.length];
             System.arraycopy(languages, 0, this.measurableLanguages, 0, languages.length);
             this.metricName = AbstractPlugin.this.getMetricName();
             this.metricType = AbstractPlugin.this.getMetricType();
             this.useClassInfo = AbstractPlugin.this.useClassInfo();
             this.useMethodInfo = AbstractPlugin.this.useMethodInfo();
             this.useFieldInfo = AbstractPlugin.this.useFieldInfo();
             this.useFileInfo = AbstractPlugin.this.useFileInfo();
             this.useMethodLocalInfo = AbstractPlugin.this.useMethodLocalInfo();
             this.description = AbstractPlugin.this.getDescription();
             this.detailDescription = AbstractPlugin.this.getDetailDescription();
         }
 
         /**
          * ̃vOC̊ȈՐPsŕԂiłΉpŁj. ftHg̎ł "Measure gNX metrics." ƕԂ
          * evOC͂̃\bhCӂɃI[o[Ch.
          * 
          * @return ȈՐ
          */
         public String getDescription() {
             return this.description;
         }
 
         /**
          * ̃vOC̏ڍאԂiłΉpŁj. ftHg̎ł͋󕶎Ԃ evOC͂̃\bhCӂɃI[o[Ch.
          * 
          * @return ڍא
          */
         public String getDetailDescription() {
             return this.detailDescription;
         }
 
         /**
          * ̃vOCgNXvł錾ԂD
          * 
          * @return v\ȌSĊ܂ޔzD
          */
         public LANGUAGE[] getMeasurableLanguages() {
             return this.measurableLanguages;
         }
 
         /**
          * ̃vOCŎw肳ꂽŗp\ł邩ԂD
          * 
          * @param language p\ł邩𒲂ׂ
          * @return p\łꍇ trueCpłȂꍇ falseD
          */
         public boolean isMeasurable(final LANGUAGE language) {
             final LANGUAGE[] measurableLanguages = this.getMeasurableLanguages();
             for (int i = 0; i < measurableLanguages.length; i++) {
                 if (language.equals(measurableLanguages[i])) {
                     return true;
                 }
             }
             return false;
         }
 
         /**
          * ̃vOCv郁gNX̖OԂD
          * 
          * @return gNX
          */
         public String getMetricName() {
             return this.metricName;
         }
 
         /**
          * ̃vOCv郁gNX̃^CvԂD
          * 
          * @return gNX^Cv
          * @see jp.ac.osaka_u.ist.sel.metricstool.main.util.METRIC_TYPE
          */
         public METRIC_TYPE getMetricType() {
             return this.metricType;
         }
 
         /**
          * ̃vOCNXɊւ𗘗p邩ǂԂD
          * 
          * @return NXɊւ𗘗pꍇtrueD
          */
         public boolean isUseClassInfo() {
             return this.useClassInfo;
         }
 
         /**
          * ̃vOCtB[hɊւ𗘗p邩ǂԂD
          * 
          * @return tB[hɊւ𗘗pꍇtrueD
          */
         public boolean isUseFieldInfo() {
             return this.useFieldInfo;
         }
 
         /**
          * ̃vOCt@CɊւ𗘗p邩ǂԂD
          * 
          * @return t@CɊւ𗘗pꍇtrueD
          */
         public boolean isUseFileInfo() {
             return this.useFileInfo;
         }
 
         /**
          * ̃vOC\bhɊւ𗘗p邩ǂԂD
          * 
          * @return \bhɊւ𗘗pꍇtrueD
          */
         public boolean isUseMethodInfo() {
             return this.useMethodInfo;
         }
 
         /**
          * ̃vOC\bhɊւ𗘗p邩ǂԂD
          * 
          * @return \bhɊւ𗘗pꍇtrueD
          */
         public boolean isUseMethodLocalInfo() {
             return this.useMethodLocalInfo;
         }
 
         private final LANGUAGE[] measurableLanguages;
 
         private final String metricName;
 
         private final METRIC_TYPE metricType;
 
         private final String description;
 
         private final String detailDescription;
 
         private final boolean useClassInfo;
 
         private final boolean useFieldInfo;
 
         private final boolean useFileInfo;
 
         private final boolean useMethodInfo;
 
         private final boolean useMethodLocalInfo;
     }
 
     /**
      * vOC̎sɋp[~bVǉ. ʌXbh炵ĂяoȂ.
      * 
      * @param permission p[~bV
      * @throws AccessControlException ʌȂXbhĂяoꍇ
      */
     public final void addPermission(final Permission permission) {
         MetricsToolSecurityManager.getInstance().checkAccess();
         this.permissions.add(permission);
     }
 
     /**
      * vOCCX^Xmr. NX̕WȂ炻pĔr. Ȃꍇ́C {@link Class}CX^X̂r.
      * Cʏ̋@\pă[hvOCNXł邱Ƃ͂肦Ȃ.
      * āCvOCNX̃CX^X͕ʂ̃NX[_烍[hĂłƔ肳.
      * 
      * @see java.lang.Object#equals(java.lang.Object)
      * @see #hashCode()
      */
     @Override
     public final boolean equals(final Object o) {
         if (o instanceof AbstractPlugin) {
             final String myClassName = this.getClass().getCanonicalName();
             final String otherClassName = o.getClass().getCanonicalName();
             if (null != myClassName && null != otherClassName) {
                 // ǂNXȂꍇ
                 return myClassName.equals(otherClassName);
             } else if (null != myClassName || null != otherClassName) {
                 // ǂ͓NXǁCǂ͈Ⴄ
                 return false;
             } else {
                 // ƂNX
                 return this.getClass().equals(o.getClass());
             }
         }
 
         return false;
     }
 
     /**
      * vOCCX^X̃nbVR[hԂ. NX̕WȂ炻̃nbVR[hg. Ȃꍇ́C {@link Class}CX^X̃nbVR[hg.
      * Cʏ̋@\pĂ[hvOCNXł邱Ƃ͂肦Ȃ.
      * āCvOCNX̃CX^X͕ʂ̃NX[_烍[hẴnbVR[hԂ.
      * 
      * @see java.lang.Object#hashCode()(java.lang.Object)
      * @see #equals(Object)
      */
     @Override
     public final int hashCode() {
         final Class myClass = this.getClass();
         final String myClassName = myClass.getCanonicalName();
         return myClassName != null ? myClassName.hashCode() : myClass.hashCode();
     }
 
     /**
      * vOC̃[gfBNgZbg xZbgꂽlύX邱Ƃ͏oȂ.
      * 
      * @param rootDir [gfBNg
      * @throws NullPointerException rootDirnull̏ꍇ
      * @throws IllegalStateException rootDirɃZbgĂꍇ
      */
     public final synchronized void setPluginRootdir(final File rootDir) {
         MetricsToolSecurityManager.getInstance().checkAccess();
 
         if (null == rootDir) {
             throw new NullPointerException("rootdir is null.");
         }
         if (null != this.pluginRootDir) {
             throw new IllegalStateException("rootdir was already set.");
         }
 
         this.pluginRootDir = rootDir;
     }
 
     /**
      * bZ[WM҂ƂĂ̖OԂ
      * 
      * @return M҂ƂĂ̖O
      * @see jp.ac.osaka_u.ist.sel.metricstool.main.plugin.connection.MessageSource#getMessageSourceName()
      */
     public String getMessageSourceName() {
         return this.sourceName;
     }
 
     /**
      * ̃vOCɋĂp[~bV̕sςȏWԂ.
      * 
      * @return ̃vOCɋĂp[~bV̏W.
      */
     public final Permissions getPermissions() {
         final Permissions permissions = new Permissions();
 
         for (final Enumeration<Permission> enumeration = this.permissions.elements(); enumeration
                 .hasMoreElements();) {
             permissions.add(enumeration.nextElement());
         }
         permissions.setReadOnly();
         return permissions;
     }
 
     /**
      * vOCۑĂ{@link PluginInfo}NX̃CX^XԂD
      * AbstractPluginCX^Xɑ΂邱̃\bh͕K̃CX^XԂC ̓ɕۑĂ͕sςłD
      * 
      * @return vOCۑĂ{@link PluginInfo}NX̃CX^X
      */
     public final PluginInfo getPluginInfo() {
         if (null == this.pluginInfo) {
             synchronized (this) {
                 if (null == this.pluginInfo) {
                     this.pluginInfo = new PluginInfo();
                     this.sourceName = this.pluginInfo.getMetricName();
                 }
             }
         }
         return this.pluginInfo;
     }
 
     /**
      * vOC̃[gfBNgԂ
      * 
      * @return vOC̃[gfBNg
      */
     public final File getPluginRootDir() {
         return this.pluginRootDir;
     }
 
     /**
      * i񑗐M҂ƂĂ̖OԂ
      * 
      * @return i񑗐M҂ƂĂ̖O
      * @see jp.ac.osaka_u.ist.sel.metricstool.main.plugin.connection.ProgressSource#getProgressSourceName()
      */
     public String getProgressSourceName() {
         return this.sourceName;
     }
 
     /**
      * vOC񂪊ɍ\zς݂ǂԂ
      * 
      * @return vOC񂪊ɍ\zς݂Ȃtrue,łȂfalse
      */
     public final boolean isPluginInfoCreated() {
         return null != this.pluginInfo;
     }
 
     /**
      * gNX͂X^[g钊ۃ\bhD
      */
     protected abstract void execute();
 
     /**
      * t@CɃANZXftHg̃ANZT擾.
      * 
      * @return t@CɃANZXftHg̃ANZT.
      */
     protected final FileInfoAccessor getFileInfoAccessor() {
         return this.fileInfoAccessor;
     }
 
     /**
      * NXɃANZXftHg̃ANZT擾.
      * 
      * @return NXɃANZXftHg̃ANZT.
      */
     protected final ClassInfoAccessor getClassInfoAccessor() {
         return this.classInfoAccessor;
     }
 
     /**
      * \bhɃANZXftHg̃ANZT擾.
      * 
      * @return \bhɃANZXftHg̃ANZT.
      */
     protected final MethodInfoAccessor getMethodInfoAccessor() {
         return this.methodInfoAccessor;
     }
 
     /**
      * ̃vOC̊ȈՐPsŕԂiłΉpŁj ftHg̎ł "Measuring the gNX metric." ƕԂ
      * evOC͂̃\bhCӂɃI[o[Ch.
      * 
      * @return ȈՐ
      */
     protected String getDescription() {
         return "Measuring the " + this.getMetricName() + " metric.";
     }
 
     /**
      * ̃vOC̏ڍאԂiłΉpŁj ftHgł͋󕶎Ԃ. evOC͂̃\bhCӂɃI[o[Ch.
      * 
      * @return
      */
     protected String getDetailDescription() {
         return "";
     }
 
     /**
      * ̃vOCgNXvł錾Ԃ pł錾ɐ̂vOĆÃ\bhI[o[ChKvD
      * 
      * @return v\ȌSĊ܂ޔz
      * @see jp.ac.osaka_u.ist.sel.metricstool.main.util.LANGUAGE
      */
     protected LANGUAGE[] getMeasurableLanguages() {
         return LANGUAGE.values();
     }
 
     /**
      * ̃vOCv郁gNX̖OԂۃ\bhD
      * 
      * @return gNX
      */
     protected abstract String getMetricName();
 
     /**
      * ̃vOCv郁gNX̃^CvԂۃ\bhD
      * 
      * @return gNX^Cv
      * @see jp.ac.osaka_u.ist.sel.metricstool.main.util.METRIC_TYPE
      */
     protected abstract METRIC_TYPE getMetricType();
 
     /**
      * t@CPʂ̃gNXlo^郁\bh.
      * 
      * @param fileInfo gNXlo^t@C
      * @param value gNXl
      * @throws MetricAlreadyRegisteredException ɂ̃vOC炱̃t@CɊւ郁gNXl̕񍐂Ăꍇ.
      */
     protected final void registMetric(final FileInfo fileInfo, final Number value)
             throws MetricAlreadyRegisteredException {
 
         if ((null == fileInfo) || (null == value)) {
             throw new NullPointerException();
         }
 
         if (null == this.fileMetricsRegister) {
             synchronized (this) {
                 if (null == this.fileMetricsRegister) {
                     this.fileMetricsRegister = new DefaultFileMetricsRegister(this);
                 }
             }
         }
         this.fileMetricsRegister.registMetric(fileInfo, value);
     }
 
     /**
      * NXPʂ̃gNXlo^郁\bh.
      * 
      * @param classInfo gNXlo^NX
      * @param value gNXl
      * @throws MetricAlreadyRegisteredException ɂ̃vOC炱̃NXɊւ郁gNXl̕񍐂Ăꍇ.
      */
     protected final void registMetric(final ClassInfo classInfo, final Number value)
             throws MetricAlreadyRegisteredException {
 
         if ((null == classInfo) || (null == value)) {
             throw new NullPointerException();
         }
 
         if (null == this.classMetricsRegister) {
             synchronized (this) {
                 if (null == this.classMetricsRegister) {
                     this.classMetricsRegister = new DefaultClassMetricsRegister(this);
                 }
             }
         }
         this.classMetricsRegister.registMetric(classInfo, value);
     }
 
     /**
      * \bhPʂ̃gNXlo^郁\bh.
      * 
      * @param methodInfo gNXlo^郁\bh
      * @param value gNXl
      * @throws MetricAlreadyRegisteredException ɂ̃vOC炱̃\bhɊւ郁gNXl̕񍐂Ăꍇ.
      */
     protected final void registMetric(final MethodInfo methodInfo, final Number value)
             throws MetricAlreadyRegisteredException {
 
         if ((null == methodInfo) || (null == value)) {
             throw new NullPointerException();
         }
 
         if (null == this.methodMetricsRegister) {
             synchronized (this) {
                 if (null == this.methodMetricsRegister) {
                     this.methodMetricsRegister = new DefaultMethodMetricsRegister(this);
                 }
             }
         }
         this.methodMetricsRegister.registMetric(methodInfo, value);
     }
 
     /**
      * ̃vOC̐i𑗂郁\bh
      * 
      * @param percentage il
      */
     protected final void reportProgress(final int percentage) {
         if (this.reporter != null) {
             this.reporter.reportProgress(percentage);
         }
     }
 
     /**
      * ̃vOCNXɊւ𗘗p邩ǂԂ\bhD ftHgłfalseԂD
      * NXɊւ𗘗pvOC͂̃\bhI[o[[htrueԂȂΐȂD
      * 
      * @return NXɊւ𗘗pꍇtrueD
      */
     protected boolean useClassInfo() {
         return false;
     }
 
     /**
      * ̃vOCtB[hɊւ𗘗p邩ǂԂ\bhD ftHgłfalseԂD
      * tB[hɊւ𗘗pvOC͂̃\bhI[o[[htrueԂȂΐȂD
      * 
      * @return tB[hɊւ𗘗pꍇtrueD
      */
     protected boolean useFieldInfo() {
         return false;
     }
 
     /**
      * ̃vOCt@CɊւ𗘗p邩ǂԂ\bhD ftHgłfalseԂD
      * t@CɊւ𗘗pvOC͂̃\bhI[o[[htrueԂȂΐȂD
      * 
      * @return t@CɊւ𗘗pꍇtrueD
      */
     protected boolean useFileInfo() {
         return false;
     }
 
     /**
      * ̃vOC\bhɊւ𗘗p邩ǂԂ\bhD ftHgłfalseԂD
      * \bhɊւ𗘗pvOC͂̃\bhI[o[[htrueԂȂΐȂD
      * 
      * @return \bhɊւ𗘗pꍇtrueD
      */
     protected boolean useMethodInfo() {
         return false;
     }
 
     /**
      * ̃vOC\bhɊւ𗘗p邩ǂԂ\bhD ftHgłfalseԂD
      * \bhɊւ𗘗pvOC͂̃\bhI[o[[htrueԂȂΐȂD
      * 
      * @return \bhɊւ𗘗pꍇtrueD
      */
     protected boolean useMethodLocalInfo() {
         return false;
     }
 
     /**
      * sŐʏĂC {@link #execute()}Ăяo.
      */
     final synchronized void executionWrapper() {
         assert (null == this.reporter) : "Illegal state : previous reporter was not removed.";
         try {
             this.reporter = new DefaultProgressReporter(this);
         } catch (final AlreadyConnectedException e1) {
             assert (null == this.reporter) : "Illegal state : previous reporter was still connected.";
         }
 
         // ̃XbhɃp[~bV悤ɗv
         MetricsToolSecurityManager.getInstance().requestPluginPermission(this);
 
         try {
             this.execute();
        } catch (final Exception e) {
             this.err.println(e);
         }
 
         if (null != this.reporter) {
             // i񍐂̏ICxg𑗂
             // vOCŊɑĂ牽ɕԂĂ
             this.reporter.reportProgressEnd();
             this.reporter = null;
         }
 
         // ̃Xbhp[~bV悤ɗv
         MetricsToolSecurityManager.getInstance().removePluginPermission(this);
     }
 
     /**
      * bZ[Wo͗p̃v^[
      */
     protected final MessagePrinter out = new DefaultMessagePrinter(this, MESSAGE_TYPE.OUT);
 
     /**
      * G[bZ[Wo͗p̃v^[
      */
     protected final MessagePrinter err = new DefaultMessagePrinter(this, MESSAGE_TYPE.ERROR);
 
     /**
      * o^Ăt@CɃANZXftHg̃ANZT.
      */
     private final FileInfoAccessor fileInfoAccessor = new DefaultFileInfoAccessor();
 
     /**
      * o^ĂNXɃANZXftHg̃ANZT.
      */
     private final ClassInfoAccessor classInfoAccessor = new DefaultClassInfoAccessor();
 
     /**
      * o^Ă郁\bhɃANZXftHg̃ANZT.
      */
     private final MethodInfoAccessor methodInfoAccessor = new DefaultMethodInfoAccessor();
 
     /**
      * t@CPʂ̃gNXlo^郌WX^.
      */
     private FileMetricsRegister fileMetricsRegister;
 
     /**
      * NXPʂ̃gNXlo^郌WX^.
      */
     private ClassMetricsRegister classMetricsRegister;
 
     /**
      * \bhPʂ̃gNXlo^郌WX^.
      */
     private MethodMetricsRegister methodMetricsRegister;
 
     /**
      * i񑗐Mp̃|[^[
      */
     private ProgressReporter reporter;
 
     /**
      * ̃vOC̎s̋p[~bV
      */
     private final Permissions permissions = new Permissions();
 
     /**
      * vOC̏ۑ{@link PluginInfo}NX̃CX^X getPluginInfo\bh̏̌Ăяoɂč쐬D
      * ȍ~ÃtB[h͏ɓCX^XQƂD
      */
     private PluginInfo pluginInfo;
 
     /**
      * vOC̃[gfBNg
      */
     private File pluginRootDir;
 
     /**
      * {@link MessageSource} {@link ProgressSource}p̖O
      */
     private String sourceName = "";
 }
