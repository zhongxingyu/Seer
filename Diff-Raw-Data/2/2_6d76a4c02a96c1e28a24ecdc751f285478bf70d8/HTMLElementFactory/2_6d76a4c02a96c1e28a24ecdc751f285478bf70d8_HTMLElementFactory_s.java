 /*
  * Copyright 2011 Tomas Schlosser
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.anadix.html;
 
 import java.math.BigInteger;
 import java.util.Properties;
 
 import org.anadix.impl.AbstractElementFactory;
 import org.drools.runtime.StatefulKnowledgeSession;
 
 
 public class HTMLElementFactory extends AbstractElementFactory {
 
 	public HTMLElementFactory(StatefulKnowledgeSession ksession) {
 		super(ksession);
 	}
 
 	public void insertElement(HtmlElement element) {
 		super.insertElement(element);
 	}
 
 	public ATag createATag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new ATag(id, parent, new Attributes(attributes));
 	}
 
 	public AbbrTag createAbbrTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new AbbrTag(id, parent, new Attributes(attributes));
 	}
 
 	public AcronymTag createAcronymTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new AcronymTag(id, parent, new Attributes(attributes));
 	}
 
 	public AddressTag createAddressTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new AddressTag(id, parent, new Attributes(attributes));
 	}
 
 	public AppletTag createAppletTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new AppletTag(id, parent, new Attributes(attributes));
 	}
 
 	public AreaTag createAreaTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new AreaTag(id, parent, new Attributes(attributes));
 	}
 
 	public BTag createBTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new BTag(id, parent, new Attributes(attributes));
 	}
 
 	public BaseTag createBaseTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new BaseTag(id, parent, new Attributes(attributes));
 	}
 
 	public BasefontTag createBasefontTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new BasefontTag(id, parent, new Attributes(attributes));
 	}
 
 	public BdoTag createBdoTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new BdoTag(id, parent, new Attributes(attributes));
 	}
 
 	public BigTag createBigTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new BigTag(id, parent, new Attributes(attributes));
 	}
 
 	public BlockquoteTag createBlockquoteTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new BlockquoteTag(id, parent, new Attributes(attributes));
 	}
 
 	public BodyTag createBodyTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new BodyTag(id, parent, new Attributes(attributes));
 	}
 
 	public BrTag createBrTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new BrTag(id, parent, new Attributes(attributes));
 	}
 
 	public ButtonTag createButtonTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new ButtonTag(id, parent, new Attributes(attributes));
 	}
 
 	public CaptionTag createCaptionTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new CaptionTag(id, parent, new Attributes(attributes));
 	}
 
 	public CenterTag createCenterTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new CenterTag(id, parent, new Attributes(attributes));
 	}
 
 	public CiteTag createCiteTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new CiteTag(id, parent, new Attributes(attributes));
 	}
 
 	public CodeTag createCodeTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new CodeTag(id, parent, new Attributes(attributes));
 	}
 
 	public ColTag createColTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new ColTag(id, parent, new Attributes(attributes));
 	}
 
 	public ColgroupTag createColgroupTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new ColgroupTag(id, parent, new Attributes(attributes));
 	}
 
 	public DdTag createDdTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new DdTag(id, parent, new Attributes(attributes));
 	}
 
 	public DelTag createDelTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new DelTag(id, parent, new Attributes(attributes));
 	}
 
 	public DfnTag createDfnTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new DfnTag(id, parent, new Attributes(attributes));
 	}
 
 	public DirTag createDirTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new DirTag(id, parent, new Attributes(attributes));
 	}
 
 	public DivTag createDivTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new DivTag(id, parent, new Attributes(attributes));
 	}
 
 	public DlTag createDlTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new DlTag(id, parent, new Attributes(attributes));
 	}
 
 	public DtTag createDtTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new DtTag(id, parent, new Attributes(attributes));
 	}
 
 	public EmTag createEmTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new EmTag(id, parent, new Attributes(attributes));
 	}
 
 	public FieldsetTag createFieldsetTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new FieldsetTag(id, parent, new Attributes(attributes));
 	}
 
 	public FontTag createFontTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new FontTag(id, parent, new Attributes(attributes));
 	}
 
 	public FormTag createFormTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new FormTag(id, parent, new Attributes(attributes));
 	}
 
 	public FrameTag createFrameTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new FrameTag(id, parent, new Attributes(attributes));
 	}
 
 	public FramesetTag createFramesetTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new FramesetTag(id, parent, new Attributes(attributes));
 	}
 
 	public H1Tag createH1Tag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new H1Tag(id, parent, new Attributes(attributes));
 	}
 
 	public H2Tag createH2Tag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new H2Tag(id, parent, new Attributes(attributes));
 	}
 
 	public H3Tag createH3Tag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new H3Tag(id, parent, new Attributes(attributes));
 	}
 
 	public H4Tag createH4Tag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new H4Tag(id, parent, new Attributes(attributes));
 	}
 
 	public H5Tag createH5Tag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new H5Tag(id, parent, new Attributes(attributes));
 	}
 
 	public H6Tag createH6Tag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new H6Tag(id, parent, new Attributes(attributes));
 	}
 
 	public HeadTag createHeadTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new HeadTag(id, parent, new Attributes(attributes));
 	}
 
 	public HrTag createHrTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new HrTag(id, parent, new Attributes(attributes));
 	}
 
	public HtmlTag createHtmlTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new HtmlTag(id, new Attributes(attributes));
 	}
 
 	public ITag createITag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new ITag(id, parent, new Attributes(attributes));
 	}
 
 	public IframeTag createIframeTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new IframeTag(id, parent, new Attributes(attributes));
 	}
 
 	public ImgTag createImgTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new ImgTag(id, parent, new Attributes(attributes));
 	}
 
 	public InputTag createInputTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new InputTag(id, parent, new Attributes(attributes));
 	}
 
 	public InsTag createInsTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new InsTag(id, parent, new Attributes(attributes));
 	}
 
 	public IsindexTag createIsindexTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new IsindexTag(id, parent, new Attributes(attributes));
 	}
 
 	public KbdTag createKbdTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new KbdTag(id, parent, new Attributes(attributes));
 	}
 
 	public LabelTag createLabelTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new LabelTag(id, parent, new Attributes(attributes));
 	}
 
 	public LinkTag createLinkTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new LinkTag(id, parent, new Attributes(attributes));
 	}
 
 	public LiTag createLiTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new LiTag(id, parent, new Attributes(attributes));
 	}
 
 	public MapTag createMapTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new MapTag(id, parent, new Attributes(attributes));
 	}
 
 	public MenuTag createMenuTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new MenuTag(id, parent, new Attributes(attributes));
 	}
 
 	public MetaTag createMetaTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new MetaTag(id, parent, new Attributes(attributes));
 	}
 
 	public NoframesTag createNoframesTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new NoframesTag(id, parent, new Attributes(attributes));
 	}
 
 	public NoscriptTag createNoscriptTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new NoscriptTag(id, parent, new Attributes(attributes));
 	}
 
 	public ObjectTag createObjectTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new ObjectTag(id, parent, new Attributes(attributes));
 	}
 
 	public OlTag createOlTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new OlTag(id, parent, new Attributes(attributes));
 	}
 
 	public OptgroupTag createOptgroupTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new OptgroupTag(id, parent, new Attributes(attributes));
 	}
 
 	public OptionTag createOptionTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new OptionTag(id, parent, new Attributes(attributes));
 	}
 
 	public PTag createPTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new PTag(id, parent, new Attributes(attributes));
 	}
 
 	public ParamTag createParamTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new ParamTag(id, parent, new Attributes(attributes));
 	}
 
 	public PreTag createPreTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new PreTag(id, parent, new Attributes(attributes));
 	}
 
 	public QTag createQTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new QTag(id, parent, new Attributes(attributes));
 	}
 
 	public STag createSTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new STag(id, parent, new Attributes(attributes));
 	}
 
 	public SampTag createSampTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new SampTag(id, parent, new Attributes(attributes));
 	}
 
 	public ScriptTag createScriptTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new ScriptTag(id, parent, new Attributes(attributes));
 	}
 
 	public SelectTag createSelectTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new SelectTag(id, parent, new Attributes(attributes));
 	}
 
 	public SmallTag createSmallTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new SmallTag(id, parent, new Attributes(attributes));
 	}
 
 	public SpanTag createSpanTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new SpanTag(id, parent, new Attributes(attributes));
 	}
 
 	public StrikeTag createStrikeTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new StrikeTag(id, parent, new Attributes(attributes));
 	}
 
 	public StrongTag createStrongTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new StrongTag(id, parent, new Attributes(attributes));
 	}
 
 	public StyleTag createStyleTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new StyleTag(id, parent, new Attributes(attributes));
 	}
 
 	public SubTag createSubTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new SubTag(id, parent, new Attributes(attributes));
 	}
 
 	public SupTag createSupTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new SupTag(id, parent, new Attributes(attributes));
 	}
 
 	public TableTag createTableTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new TableTag(id, parent, new Attributes(attributes));
 	}
 
 	public TbodyTag createTbodyTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new TbodyTag(id, parent, new Attributes(attributes));
 	}
 
 	public TdTag createTdTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new TdTag(id, parent, new Attributes(attributes));
 	}
 
 	public TextareaTag createTextareaTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new TextareaTag(id, parent, new Attributes(attributes));
 	}
 
 	public TfootTag createTfootTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new TfootTag(id, parent, new Attributes(attributes));
 	}
 
 	public TheadTag createTheadTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new TheadTag(id, parent, new Attributes(attributes));
 	}
 
 	public ThTag createThTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new ThTag(id, parent, new Attributes(attributes));
 	}
 
 	public TitleTag createTitleTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new TitleTag(id, parent, new Attributes(attributes));
 	}
 
 	public TrTag createTrTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new TrTag(id, parent, new Attributes(attributes));
 	}
 
 	public TtTag createTtTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new TtTag(id, parent, new Attributes(attributes));
 	}
 
 	public UTag createUTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new UTag(id, parent, new Attributes(attributes));
 	}
 
 	public UlTag createUlTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new UlTag(id, parent, new Attributes(attributes));
 	}
 
 	public VarTag createVarTag(BigInteger id, HtmlElement parent, Properties attributes) {
 		return new VarTag(id, parent, new Attributes(attributes));
 	}
 
 	public HtmlElement createTag(BigInteger id, String name, HtmlElement parent, Properties attributes) {
 		return new DefaultElement(id, name, parent, new Attributes(attributes));
 	}
 
 	public static class DefaultElement extends HtmlElement {
 		public DefaultElement(BigInteger id, String name, HtmlElement parent, Attributes attributes) {
 			super(id, name, parent, attributes);
 		}
 	}
 }
